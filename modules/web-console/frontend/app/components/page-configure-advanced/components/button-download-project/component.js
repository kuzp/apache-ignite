import template from './template.pug';

export class ButtonDownloadProject {
    static $inject = ['ConfigurationDownload'];
    constructor(ConfigurationDownload) {
        Object.assign(this, {ConfigurationDownload});
    }
    download() {
        return this.ConfigurationDownload.downloadClusterConfiguration(this.cluster);
    }
}
export const component = {
    name: 'buttonDownloadProject',
    controller: ButtonDownloadProject,
    template,
    bindings: {
        cluster: '<'
    }
};
