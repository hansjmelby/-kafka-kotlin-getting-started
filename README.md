# kafka-kotlin-getting-started

Prosjektet er et eksemepel på bruk av kafka som integrasjonsplattform mellom flere microtjenester

Alle tjenestene er skrever i Kotlin.

# Public Hendelser


| TOPIC                 | Eier  | SCHEMA                  | Beskrivelse                                    |
|-----------------------|-------|-------------------------|------------------------------------------------|
| KUNDE_OPPRETTET       | KUNDE | {<br/>"id:":1234"<br/>} | Hendelse som sends når en kunde blir opprettet |
| KUNDE_STATUS_ENDRET   | KUNDE | {<br/>"kundeID": "12345678901",<br/>"status":"DODGY"<br/>}| når status på kunde endres                     |
| ORDRE_STATUS_ENDRET   | SALG  | {<br/>"ordreID" : 1,<br/>"kundeID" : "12345678901",<br/>"status" : "REGISTRERT"<br/>}| Når status på ordre endres  |

## Test av publisering
Vil du teste av produsenten din funger, så kan du punblisere til topic <b>TEST</b> da ingen konsumerer fra dette i dag


# kjøring lokalt
## Krav til miljø
* Java
* kotlin
* gradle
* Intelij (anbefalt)

klassen MonolothApplication starer en "monolitt" som innholder alle tjenester og rest endepunkter som trengs for å kjøre hele verdikjeden lokalt.
Dersom det er ønskelig å ha flere microtjenester så kan man taa utgangspunkt i MonolothApplication og kommentere ut alt utenom en tjeneste, samt endre port nummer dersom man skal kjøre opp tjenestene lokalt samtidig

Klassen AppProduser er en klasse som er ment å simulere en verdikjede på kafka og kan brukes til å trigge hendelser på kafka


Dersom du vil starte egen kafka instans (lokalt) kan du gjøre det via docker :
docker run -e ADV_HOST=127.0.0.1 -e EULA="https://licenses.lenses.io/d/?id=b8467e70-4729-11ee-8f1e-42010af01003" --rm -p 3030:3030 -p 9092:9092 lensesio/box:latest