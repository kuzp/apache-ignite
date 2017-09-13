import templateUrl from 'views/configuration/caches.tpl.pug';
import controller from 'Controllers/caches-controller';

export default {
    name: 'pageConfigureAdvancedCaches',
    templateUrl,
    controller,
    bindings: {
        originalCache: '<cache',
        clusterItems: '<',
        itemToEdit: '<',
        isNew: '<',
        onAdvancedSave: '&',
        onItemAdd: '&',
        onItemChange: '&',
        onItemRemove: '&',
        onEditCancel: '&'
    }
};
