# VLO 5.0 prototype

Collection of service prototypes, Spring (Boot) based.

## Getting started quickly notes

### Build

Build with maven profile `dev`.

API tests only work if an ElasticSearch index is available. Start the service
with the docker compose project in the `index` directory. Alternatively, skip 
tests.

### Starting

The API is the central component. Always run it first. By default it runs on
port 8708.

### Configuration

Different profiles are available to run in elastic search mode or solr mode.
There may be other modes. Solr mode can be used to run the API with the existing
VLO 4.x Solr as a back end. Write does not work in that case.

When running in dev mode only, override configuration with a yaml file at
`~/.config/spring-boot/spring-boot-devtools.yml`. Example config that could be
used as a starting point: 

```yaml
logging:
 level:
   eu.clarin.cmdi.vlo.batchimporter: DEBUG
   eu.clarin.cmdi.vlo.api: DEBUG
   eu.clarin.cmdi.vlo.web: DEBUG
   eu.clarin.cmdi.vlo.mapping: DEBUG
#    org.springframework.batch: DEBUG

# spring.profiles.active=solr
spring:
  profiles:
    include:
      - "solr"
#      - elastic
#      - elastic-test
  application:
    name: vlo-api
  data:
    elasticsearch:
      client:
        reactive:
          endpoints: 127.0.0.1:9200
          username: admin
          password: admin
          use-ssl: false
  elasticsearch:
    rest:
      uris: http://admin:admin@127.0.0.1:9200
  devtools:
    livereload:
        enabled: false
vlo:
  importer:
    metadata-source:
      roots: 
        - name: 'Small test set'
          path: /Users/twagoo/vlo/files/small_test_set
  api:
    security:
      admin:
        username: 'admin'
        password: '{bcrypt}SET_AN_ACTUAL_PASSWORD_HASH'
    mapping:
      definitionUri: 'file:/Users/twagoo/Documents/Repositories/git/VLO-5.0/vlo-5.0/vlo-api/src/test/resources/test-mapping-definition.xml'
    legacy:
      config-location: '/Users/twagoo/vlo/vlo3/VloConfig.xml'
    cache:
      stats: true
      search:
        ttl:
          seconds: 30
      record:
        ttl:
          seconds: 30
      facet:
        ttl:
          seconds: 30

## remote (alpha)
solr:
  url: 'https://alpha-vlo.clarin.eu/solr/vlo-index/'
  auth:
    username: 'ASK_FOR_USERNAME'
    password: 'ASK_FOR_PASSWORD'
```

