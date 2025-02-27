# SSI Kit

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=security_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit&metric=ncloc)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=walt-id_waltid-ssikit-examples&metric=alert_status)](https://sonarcloud.io/dashboard?id=walt-id_waltid-ssikit)
  
[![CI/CD Workflow for walt.id SSI Kit](https://github.com/walt-id/waltid-ssikit/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/walt-id/waltid-ssikit/actions/workflows/build.yml)

The **SSI Kit** by **walt.id** is a holistic Self-Sovereign-Identity solution, with primarily focus on the European EBSI/ESSIF ecosystem.

The core services are in the scope of:
 - **Key Management** generation, import/export
 - **Decentralized Identifier (DID)** operations (create, register, update, deactivate)
 - **Verifiable Credential (VC)** operations (issue, present, verify)
 - **EBSI/ESSIF** related Use Cases (onboarding, VC exchange, etc.)

The EBSI/ESSIF functions are in the scope of:
 - **Onboarding EBSI/ESSIF** onboarding a natural person/legal entity including the DID creation and registration
 - **Enable Trusted Issuer** process for entitling a legal entity to become a Trusted Issuer in the ESSIF ecosystem.
 - **Credential Issuance** protocols and data formats for issuing W3C credentials from a Trusted Issuer to a natural person.
 - **Credential Verification** verification facilities in order to determine the validity of a W3C Verifiable Credential aligned with EBSI/ESSIF standards.

The library is written in **Kotlin/Java** and can be directly integrated as Maven/Gradle dependency. Alternatively the library or the additional **Docker container** can be run as RESTful webservice.

## Getting Started

### Via Docker

The easiest way to getting your hands dirty and to "play" with the functions the SSI Kit provides is by running the **CLI tool** with _Docker_.

    docker run -itv $(pwd)/data:/app/data waltid/ssikit -h

### As dependency

_Gradle_

        implementation("id.walt:waltid-ssi-kit:1.11.0")

_Maven_

        <dependency>
            <groupId>id.walt</groupId>
            <artifactId>waltid-ssi-kit</artifactId>
            <version>1.11.0</version>
        </dependency>
    
Please go ahead and find further CLI commands and well as other ways how to use the SSI Kit in the documentation section below.

## Documentation

The documentation is hosted at: https://docs.walt.id/ssikit/

Direct links for using the SSI Kit are:

- Quick Start (Build & run the SSI Kit with Docker or with **ssikit.sh**): https://docs.walt.id/v/ssikit/getting-started/quick-start
- Docs: https://docs.walt.id/v/ssikit/ssi-kit/ssi-kit
- CLI Tool: https://docs.walt.id/v/ssikit/getting-started/cli-command-line-interface
- APIs: https://docs.walt.id/v/ssikit/getting-started/rest-apis

## Examples

This project demonstrates how to integrate & use the SSI Kit in any Kotlin/Java app: https://github.com/walt-id/waltid-ssikit-examples. Also the **Gradle** and **Maven** build instructions are provided there.

Following code snipped gives a first impression how to use the SSI Kit for creating **W3C Decentralized Identifiers** and for issuing/verifying **W3C Verifiable Credentials** in **JSON_LD** as well as **JWT** format.

    fun main() {

        ServiceMatrix("service-matrix.properties")
    
        val issuerDid = DidService.create(DidMethod.ebsi)
        val holderDid = DidService.create(DidMethod.key)
    
        // Issue VC in JSON-LD and JWT format (for show-casing both formats)
        val vcJson = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.LD_PROOF))
        val vcJwt = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.JWT))
    
        // Present VC in JSON-LD and JWT format (for show-casing both formats)
        val vpJson = Custodian.getService().createPresentation(listOf(vcJson), holderDid)
        val vpJwt = Custodian.getService().createPresentation(listOf(vcJwt), holderDid)
    
        // Verify VPs, using Signature, JsonSchema and a custom policy
        val resJson = Auditor.getService().verify(vpJson, listOf(SignaturePolicy(), JsonSchemaPolicy()))
        val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy()))
    
        println("JSON verification result: ${resJson.overallStatus}")
        println("JWT verification result: ${resJwt.overallStatus}")
    }

## Relevant Standards

- EBSI Wallet Conformance https://ec.europa.eu/digital-building-blocks/wikis/display/EBSIDOC/EBSI+Wallet+Conformance+Testing
- Verifiable Credentials Data Model 1.0 https://www.w3.org/TR/vc-data-model/
- Decentralized Identifiers (DIDs) v1.0 https://w3c.github.io/did-core/
- DID Method Rubric https://w3c.github.io/did-rubric/
- did:web Decentralized Identifier Method Specification https://w3c-ccg.github.io/did-method-web/
- The did:key Method v0.7 https://w3c-ccg.github.io/did-method-key/

## Funded & supported by

<a href="https://essif-lab.eu/" target="_blank"><img src="logos-supporter.png"></a>

## License

```
Copyright ((C)) 2022 walt.id GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
