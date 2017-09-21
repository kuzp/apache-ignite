import angular from 'angular';
import {component} from './component';
import service from './service';
import {component as stepIndicator} from './step-indicator/component';

export default angular
.module('configuration.modal-import-models', [])
.service(service.name, service)
.component(stepIndicator.name, stepIndicator)
.component(component.name, component);
