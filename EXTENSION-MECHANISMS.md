# Introduction
The goal of this document is to introduce you to the extension mechanisms that help to extend to global code with OE specific attributes without affecting the global code significantly.
The basic idea to achieve this is by keeping the extensions in separate classes and configuration files. This also helps to prevent (merge-) conflicts later on.

**Disclaimer:** This document is only about adding new properties to the existing datamodel, not about adding new endpoints or services.
This can be done by e.g. introducing a new submodule following the same concept but this isn't covered by this document.

# Basic Concept
Based on the fact that the complete BFF-Architecture is based upon the API-datamodel we need to have different maven-artifacts depending on which OE it is built for.

# Setup the structure
In order to achieve this the parent-pom.xml got a new property `oeSuffix`. When the code is managed in separate branches this can be customized to e.g. `-oe1` to build a OE-specific artifact with version `0.0.1-oe1`. When every OE is managed in the same branch this property can passed from the commandline like `mvn install -DoeSuffix=-oe1`.
```
<properties>
  <revision>0.0.1${oeSuffix}</revision>
  <oeSuffix>-oe1</oeSuffix>
  ...
</properties>
```
Every (sub-)module and their dependency management also needs to refer to it
```
<version>${revision}</version>
```

So now we have different maven-artifacts for every OE, we can customize our code. There are two parts that need to be considered:
1. Extension of the datamodel
2. Extension of the CISL-mapping


## 1. Extension of the API / datamodel
The datamodel is completely managed in the submodule `itmp-mo-api` by yaml-files. The OE-specific extensions are managed in file `itmp-mo-models-extension.yaml` for global or in `itmp-mo-models-extension${oeSuffix}.yaml` (e.g. `itmp-mo-models-extension-oe1.yaml`) for a specific OE.

In Order to have an extension of an object there needs to be a line added referring to the extension file. In the best case this is already present:
```
    ContractAddressDetails:
      type: object
      allOf:
        - $ref: 'itmp-mo-models-extension@oeSuffix@.yml#/components/schemas/ContractAddressDetailsExtension'
        - $ref: '#/components/schemas/OfferingAddressDetails'
```
If it has been newly added, an empty object needs to be added to all extension files
```
components:
  schemas:
    ContractAddressDetailsExtension:
      properties:
```

Now we can simpy add properties to the OE-specific file e.g. `province` in my example
```
components:
  schemas:
    ContractAddressDetailsExtension:
      properties:
        province:
          type: string
```

That's it for the API / datamodel. It now recognizes the new attribute(s) at generates according classes and swagger files.
Next step is to extend the CISL-mapping.


## 2. Extension of CISL mapping
The mapping of objects between the API and CISL should be organized in simple Spring-Beans and having a mapping function for every object.
```
@Service
public class PartyMapperImpl implements PartyMapper {
  public Address toCislAddress(AddressDetails address) {
    Address cisl = new AddressImpl();
    cisl.setStreet(address.getStreet());
    cisl.setCity(address.getCity());
    // ...
    return cisl;
  }
}
```
Having the mappings organized like that, its possible to simply override the implementation and add extensions as needed.
```
@Service
@Primary
public class PartyMapperOe1Impl extends PartyMapperImpl {
  @Override
  public Address toCislAddress(AddressDetails address) {
    Address cisl = super.toCislAddress(address);
    cisl.setProvince(address.getProvince());
    return cisl;
  }
}
```
When the code is organized in different branches for every OE it's simple as that.
When all OEs are managed in the same branch things are getting a bit more complicated to avoid collisions at compile time between different OEs. To solve this it is possible to create a submodule with multiple source folders like `src/main/java` and `src/main/java-oe1` and having the pom.xml resolve the folder by using `${oeSuffix}`
