import template from './template.pug';
import './style.scss';
// import find from 'lodash/find';
// import get from 'lodash/fp/get';

const IMPORT_DM_NEW_CACHE = 1;

export class TablesActionCell {
    static $inject = ['$element'];
    constructor($element) {
        Object.assign(this, {$element});
    }
    onClick(e) {
        e.stopPropagation();
    }
    $postLink() {
        this.$element.on('click', this.onClick);
    }
    $onDestroy() {
        this.$element.off('click', this.onClick);
        this.$element = null;
    }
    tableActionView(table) {
        if (!this.caches) return;
        const cache = this.caches.find((c) => c.value === table.cacheOrTemplate);
        if (!cache) return;
        const cacheName = cache.label;
        // const cacheName = get('label')(find({value: table.cacheOrTemplate}));

        if (table.action === IMPORT_DM_NEW_CACHE)
            return 'Create ' + table.generatedCacheName + ' (' + cacheName + ')';

        return 'Associate with ' + cacheName;
    }
}

export const component = {
    name: 'tablesActionCell',
    controller: TablesActionCell,
    bindings: {
        onEditStart: '&',
        table: '<',
        caches: '<',
        importActions: '<'
    },
    template
};
