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

class Controller {
    static $inject = ['$element', '$window'];
    ignoredClasses = [];
    handlerCheckFocusOut = (e) => {
        if (this.shouldPropagate(e)) this.igniteOnFocusOut();
    };

    constructor($element, $window) {
        Object.assign(this, {$element, $window});
    }
    $onDestroy() {
        this.$window.removeEventListener('click', this.handlerCheckFocusOut, true);
        this.$window.removeEventListener('focusin', this.handlerCheckFocusOut, true);
        this.$element = this.$window = this.handlerCheckFocusOut = null;
    }
    shouldPropagate(e) {
        return !this.targetHasIgnoredClasses(e) && this.targetIsOutOfElement(e);
    }
    targetIsOutOfElement(e) {
        return !this.$element.find(e.target).length;
    }
    targetHasIgnoredClasses(e) {
        return this.ignoredClasses.some((c) => e.target.classList.contains(c));
    }
    $onChanges(changes) {
        if (
            'ignoredClasses' in changes &&
            changes.ignoredClasses.currentValue !== changes.ignoredClasses.previousValue
        )
            this.ignoredClasses = changes.ignoredClasses.currentValue.split(' ');

    }
    $postLink() {
        this.$window.addEventListener('click', this.handlerCheckFocusOut, true);
        this.$window.addEventListener('focusin', this.handlerCheckFocusOut, true);
    }
}

export default function() {
    return {
        controller: Controller,
        bindToController: {
            igniteOnFocusOut: '&',
            ignoredClasses: '@?igniteOnFocusOutIgnoredClasses'
        }
    };
}
