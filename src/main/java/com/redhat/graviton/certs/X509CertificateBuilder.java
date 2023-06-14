package com.redhat.graviton.certs;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Provider;


// TODO: Try to rewrite this with less provider-specific calls/classes/logic.

@Dependent
public class X509CertificateBuilder {
    private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    private final CertificateAuthority certificateAuthority;

    private String distinguishedName;
    private String subjectAltName;
    private Instant validAfter;
    private Instant validUntil;
    private KeyPair keyPair;
    private BigInteger certSerial;
    private List<X509Extension> certExtensions;


    @Inject
    public X509CertificateBuilder(CertificateAuthority certificateAuthority) {
        this.certificateAuthority = Objects.requireNonNull(certificateAuthority);

        this.certExtensions = new ArrayList<>();
    }

    public X509CertificateBuilder setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
        return this;
    }

    public X509CertificateBuilder setSubjectAltName(String subjectAltName) {
        this.subjectAltName = subjectAltName;
        return this;
    }

    public X509CertificateBuilder setValidAfter(Instant validAfter) {
        this.validAfter = validAfter;
        return this;
    }

    public X509CertificateBuilder setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    // TODO: Maybe add an alias for setting the start/end timestamps based on what cert/crypto APIs
    // tend to call them (not-before/not-after)

    public X509CertificateBuilder setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
        return this;
    }

    public X509CertificateBuilder setCertificateSerial(BigInteger certSerial) {
        this.certSerial = certSerial;
        return this;
    }

    public X509CertificateBuilder setCertificateSerial(long certSerial) {
        this.certSerial = BigInteger.valueOf(certSerial);
        return this;
    }

    public X509CertificateBuilder generateCertificateSerial() {
        long serial;

        // Impl note:
        // Math.abs cannot negate MIN_VALUE, so we'll generate a new value when that happens.
        do {
            serial = new SecureRandom().nextLong();
        }
        while (serial == Long.MIN_VALUE);

        return this.setCertificateSerial(serial);
    }

    public X509CertificateBuilder addExtension(X509Extension extension) {
        if (extension != null) {
            this.certExtensions.add(extension);
        }

        return this;
    }

    public X509CertificateBuilder addExtensions(Collection<X509Extension> extensions) {
        if (extensions != null) {
            this.certExtensions.addAll(extensions);
        }

        return this;
    }

    /**
     * Checks the state of this builder, ensuring required fields have been set before attempting to
     * build the certificate.
     *
     * @throws IllegalStateException
     *  if one or more required fields have not been populated
     */
    private void checkBuilderState() {
        if (this.distinguishedName == null || this.distinguishedName.isEmpty()) {
            throw new IllegalStateException("distinguished name has not been set");
        }

        if (this.validAfter == null) {
            throw new IllegalStateException("validAfter/notBefore has not been set");
        }

        if (this.validUntil == null) {
            throw new IllegalStateException("validUntil/notAfter has not been set");
        }

        if (this.certSerial == null) {
            throw new IllegalStateException("certificate serial number has not been set");
        }

        if (this.keyPair == null) {
            throw new IllegalStateException("client key pair has not been set");
        }
    }

    public X509Certificate build() {
        // Verify we have all the data we need
        this.checkBuilderState();

        try {
            X509Certificate caCertificate = this.certificateAuthority.getCACertificate();
            PublicKey clientPubKey = this.keyPair.getPublic();

            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                X500Name.getInstance(caCertificate.getSubjectX500Principal().getEncoded()),
                this.certSerial,
                Date.from(this.validAfter),
                Date.from(this.validUntil),
                new X500Name(this.distinguishedName),
                SubjectPublicKeyInfo.getInstance(clientPubKey.getEncoded()));

            this.addSSLCertificateType(builder);
            this.addKeyUsage(builder);
            this.addAuthorityKeyIdentifier(builder, caCertificate);
            this.addSubjectKeyIdentifier(builder, clientPubKey);
            this.addSubjectAltName(builder, this.distinguishedName, this.subjectAltName);
            this.addExtensions(builder, this.certExtensions);

            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .setProvider(PKIUtility.PROVIDER)
                .build(this.certificateAuthority.getCAPrivateKey());

            return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
        }
        catch (IOException ioe) {
            // TODO: actually handle this exception
            throw new RuntimeException(ioe);
        }
        catch (GeneralSecurityException gse) {
            // TODO: actually handle this exception
            throw new RuntimeException(gse);
        }
        catch (OperatorCreationException oce) {
            // TODO: Turn this into a propert certificate exception
            throw new RuntimeException(oce);
        }
    }

    private void addSSLCertificateType(X509v3CertificateBuilder builder) throws IOException {
        NetscapeCertType type = new NetscapeCertType(NetscapeCertType.sslClient | NetscapeCertType.smime);
        builder.addExtension(MiscObjectIdentifiers.netscapeCertType, false, type);
    }

    private void addKeyUsage(X509v3CertificateBuilder builder) throws IOException {
        KeyUsage usage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment |
            KeyUsage.dataEncipherment);

        builder.addExtension(Extension.keyUsage, false, usage);

        ExtendedKeyUsage exUsage = new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth);
        builder.addExtension(Extension.extendedKeyUsage, false, exUsage);
    }

    private void addAuthorityKeyIdentifier(X509v3CertificateBuilder builder, X509Certificate caCertificate)
        throws IOException, GeneralSecurityException {

        AuthorityKeyIdentifier aki = new JcaX509ExtensionUtils()
            .createAuthorityKeyIdentifier(caCertificate);

        builder.addExtension(Extension.authorityKeyIdentifier, false, aki.getEncoded());
    }

    private void addSubjectKeyIdentifier(X509v3CertificateBuilder builder, PublicKey publicKey)
        throws IOException, GeneralSecurityException {

        SubjectKeyIdentifier ski = new JcaX509ExtensionUtils()
            .createSubjectKeyIdentifier(publicKey);

        builder.addExtension(Extension.subjectKeyIdentifier, false, ski.getEncoded());
    }

    private void addSubjectAltName(X509v3CertificateBuilder builder, String distinguishedName,
        String subjectAltName) throws IOException {

        if (subjectAltName == null || subjectAltName.isEmpty()) {
            return;
        }

        // Comments from CP:
        // Why add the certificate subject again as an alternative name?  RFC 6125 Section 6.4.4
        // stipulates that if SANs are provided, a validator MUST use them instead of the certificate
        // subject.  If no SANs are present, the RFC allows the validator to use the subject field.  So,
        // if we do have an SAN to add, we need to add the subject field again as an SAN.
        //
        // See:
        //  - http://stackoverflow.com/questions/5935369
        //  - https://tools.ietf.org/html/rfc6125#section-6.4.4

        GeneralName subject = new GeneralName(GeneralName.directoryName, distinguishedName);
        GeneralName name = new GeneralName(GeneralName.directoryName, "CN=" + subjectAltName);
        ASN1Encodable[] altNameArray = { subject, name };

        GeneralNames altNames = GeneralNames.getInstance(new DERSequence(altNameArray));
        builder.addExtension(Extension.subjectAlternativeName, false, altNames);
    }

    private void addExtensions(X509v3CertificateBuilder builder, Collection<X509Extension> extensions)
        throws IOException {

        for (X509Extension extension : extensions) {
            ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(extension.getOid());
            ASN1Encodable extValue;

            // TODO: Clean this up. Using instanceof implies something better could be done; but time
            // is very much against me. A builder pattern with crypto-impl specific conversion would
            // probably be cleanest here. That failing, go with some generics and a .getType() method.
            if (extension instanceof X509StringExtension) {
                String value = ((X509StringExtension) extension).getValue();
                if (value == null) {
                    value = "";
                }

                extValue = new DERUTF8String(value);
            }
            else if (extension instanceof X509ByteExtension) {
                byte[] value = ((X509ByteExtension) extension).getValue();
                if (value == null) {
                    value = new byte[0];
                }

                extValue = new DEROctetString(value);
            }
            else {
                throw new IllegalArgumentException("Unknown extension: " + extension.getClass());
            }

            builder.addExtension(oid, extension.isCritical(), extValue);
        }
    }

}
