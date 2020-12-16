# VLO monitor

## Configuration

Configuration will be loaded from a file `vlo-monitor.properties` in the working
directory. Example configuration:

```
spring.datasource.url=jdbc:h2:file:/var/vlo/vlo-monitoring-db
vlo.monitor.rules.facetValuesDecreaseWarning.collection=10%
vlo.monitor.rules.facetValuesDecreaseWarning._oaiEndpointURI=10%
vlo.monitor.rules.facetValuesDecreaseError.collection=80%
vlo.monitor.rules.totalRecordsDecreaseWarning=10%
vlo.monitor.rules.totalRecordsDecreaseError=90%
```
