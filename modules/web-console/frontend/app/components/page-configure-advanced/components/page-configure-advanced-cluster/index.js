import angular from 'angular';
import component from './component';
import service from './service';

export default angular
    .module('ignite-console.page-configure-advanced.clusters', [])
    .service('PageConfigureAdvancedCluster', service)
    .component(component.name, component);
