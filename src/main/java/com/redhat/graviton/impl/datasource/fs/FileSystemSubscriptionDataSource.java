package com.redhat.graviton.impl.datasource.fs;

import com.redhat.graviton.api.datasource.SubscriptionDataSource;
import com.redhat.graviton.api.datasource.model.ExtSubscription;
import com.redhat.graviton.impl.datasource.fs.model.FileSystemExtSubscription;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


// This file exists for demo purposes only. Don't ship this, and you probably shouldn't even be
// looking at it. Bad code is bad.

@ApplicationScoped
public class FileSystemSubscriptionDataSource implements SubscriptionDataSource {
    private static final Logger LOG = Logger.getLogger(FileSystemSubscriptionDataSource.class);

    private static final String BASE_PATH = "/home/crog/devel/subscription_data/subscriptions";

    @Override
    public List<ExtSubscription> getSubscriptions(String orgId) {
        return this.readSubscriptionsImpl(orgId, null);
    }

    @Override
    public List<ExtSubscription> getSubscriptionsByIds(String orgId, String... subIds) {
        return this.getSubscriptionsByIds(orgId, subIds != null ? Arrays.asList(subIds) : null);
    }

    @Override
    public List<ExtSubscription> getSubscriptionsByIds(String orgId, Collection<String> subIds) {
        return this.readSubscriptionsImpl(orgId, subIds != null ? new HashSet<>(subIds) : null);
    }

    private List<ExtSubscription> readSubscriptionsImpl(String orgId, Set<String> subIdFilter) {
        try {
            File dir = new File(BASE_PATH);
            if (!dir.canRead() || !dir.isDirectory()) {
                throw new IllegalStateException("BASE_PATH is not readable or not a directory: " + BASE_PATH);
            }

            List<ExtSubscription> result = new ArrayList<>();

            File subData = new File(dir, orgId + ".txt");
            if (!subData.canRead() || !subData.isFile()) {
                LOG.warnf("org has no upstream data: %s", orgId);
                return result;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(subData))) {
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    ExtSubscription sub = this.parseSubscriptionData(line);
                    result.add(sub);
                }
            }

            // Casually load all the things and then discard what we don't like. Wholly inefficient. Hooray!
            if (subIdFilter != null && !subIdFilter.isEmpty()) {
                result = result.stream()
                    .filter(sub -> subIdFilter.contains(sub.getId()))
                    .collect(Collectors.toList());
            }

            return result;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

// 2c949b85843469e8018479b6874616db        683401  NORMAL  2022-11-15 05:00:00     2023-11-15 04:59:59     RH00069 NULL    11414210        NULL
// 2c949b85843469e8018479b6874e16dd        683401  NORMAL  2022-11-15 05:00:00     2023-11-15 04:59:59     RH00798 NULL    11414210        NULL

    private Instant parseTimestamp(String timestamp) {
        return Instant.parse(timestamp.replace(" ", "T") + 'Z');
    }

    private ExtSubscription parseSubscriptionData(String data) {
        String[] chunks = data.split("\t");
        if (chunks.length != 9) {
            LOG.errorf("Invalid subscription data: %s", data);
            throw new IllegalStateException("Malformed subscription data received");
        }

        ExtSubscription sub = new FileSystemExtSubscription()
            .setId(chunks[0])
            .setOrganizationId(chunks[1])
            .setType(chunks[2])
            .setStartDate(this.parseTimestamp(chunks[3]))
            .setEndDate(this.parseTimestamp(chunks[4]))
            .setProductId(chunks[5])
            .setContractNumber(chunks[6])
            .setAccountNumber(chunks[7])
            .setOrderNumber(chunks[8]);

        return sub;
    }

}