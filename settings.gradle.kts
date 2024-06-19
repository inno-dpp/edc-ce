rootProject.name = "edc-extensions"

include(":connector")
include(":extensions:broker-server")
include(":extensions:broker-server-api:api")
include(":extensions:broker-server-api:client")
include(":extensions:broker-server-postgres-flyway-jooq")
include(":extensions:edc-ui-config")
include(":extensions:last-commit-info")
include(":extensions:policy-always-true")
include(":extensions:policy-referring-connector")
include(":extensions:policy-time-interval")
include(":extensions:postgres-flyway")
include(":extensions:sovity-messenger")
include(":extensions:sovity-edc-extensions-package")
include(":extensions:test-backend-controller")
include(":extensions:transfer-process-status-checker")
include(":extensions:wrapper:clients:java-client")
include(":extensions:wrapper:clients:java-client-example")
include(":extensions:wrapper:wrapper")
include(":extensions:wrapper:wrapper-api")
include(":extensions:wrapper:wrapper-common-api")
include(":extensions:wrapper:wrapper-common-mappers")
include(":extensions:wrapper:wrapper-ee-api")
include(":launchers:common:auth-daps")
include(":launchers:common:auth-mock")
include(":launchers:common:base")
include(":launchers:common:base-mds")
include(":launchers:common:observability")
include(":launchers:connectors:broker-server-ce")
include(":launchers:connectors:broker-server-dev")
include(":launchers:connectors:mds-ce")
include(":launchers:connectors:sovity-ce")
include(":launchers:connectors:sovity-dev")
include(":launchers:connectors:test-backend")
include(":tests")
include(":utils:catalog-parser")
include(":utils:jooq-database-access")
include(":utils:json-and-jsonld-utils")
include(":utils:test-connector-remote")
include(":utils:test-utils")
include(":utils:versions")
include("extensions:sovity-messenger:demo-maybe-here")
findProject(":extensions:sovity-messenger:demo-maybe-here")?.name = "demo-maybe-here"
