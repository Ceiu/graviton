package com.redhat.graviton.api.candlepin.model;



public class CPAttribute {

    private String name;
    private String value;

    public String getName() {
      return this.name;
   }

   public CPAttribute setName(String name) {
      this.name = name;
      return this;
   }

    public String getValue() {
      return this.value;
   }

   public CPAttribute setValue(String value) {
      this.value = value;
      return this;
   }

}
