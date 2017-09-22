import angular from 'angular';
import {component} from './component';
import service from './service';
import {component as stepIndicator} from './step-indicator/component';
import {component as tablesActionCell} from './tables-action-cell/component';
import {component as amountIndicator} from './selected-items-amount-indicator/component';

export default angular
.module('configuration.modal-import-models', [])
.service(service.name, service)
.component(tablesActionCell.name, tablesActionCell)
.component(stepIndicator.name, stepIndicator)
.component(amountIndicator.name, amountIndicator)
.component(component.name, component);
