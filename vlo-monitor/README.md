# VLO monitor

## Configuration

Configuration will be loaded from a file `vlo-monitor.properties` in the working
directory. Example configuration:

```
spring.datasource.url=jdbc:h2:file:/var/vlo/vlo-monitoring-db

vlo.monitor.config.url=/opt/vlo/config/VloConfig.xml

vlo.monitor.rules.fieldValuesDecreaseWarning.collection=10%
vlo.monitor.rules.fieldValuesDecreaseWarning._oaiEndpointURI=10%
vlo.monitor.rules.fieldValuesDecreaseError.collection=80%
vlo.monitor.rules.totalRecordsDecreaseWarning=10%
vlo.monitor.rules.totalRecordsDecreaseError=90%
```
