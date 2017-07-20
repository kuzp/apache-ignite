/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import template from './template.pug';
import './style.scss';

export default function pcUiGridFilters(uiGridConstants) {
    return {
        require: 'uiGrid',
        link: {
            pre(scope, el, attr, grid) {
                if (!grid.grid.options.enableFiltering) return;
                grid.grid.options.columnDefs.filter((cd) => cd.multiselectFilterOptions).forEach((cd) => {
                    cd.headerCellTemplate = template;
                    cd.filter = {
                        type: uiGridConstants.filter.SELECT,
                        term: cd.multiselectFilterOptions.map((t) => t.value),
                        condition(searchTerm, cellValue, row, column) {
                            return searchTerm.includes(cellValue);
                        },
                        selectOptions: cd.multiselectFilterOptions,
                        $$selectOptionsMapping: cd.multiselectFilterOptions.reduce((a, v) => Object.assign(a, {[v.value]: v.label}), {}),
                        $$multiselectFilterTooltip() {
                            return `Active filter: ${
                                this.selectOptions.length === this.term.length
                                    ? 'show all'
                                    : this.term.map((t) => this.$$selectOptionsMapping[t]).join(', ')
                            }`;
                        }
                    };
                });
            }
        }
    };
}

pcUiGridFilters.$inject = ['uiGridConstants'];
