/**
 * Created by Jagrut on 28-06-2017.
 */

(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .config(compileProvider);

    compileProvider.$inject = ['$compileProvider'];

    function compileProvider($compileProvider) {
        $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);
    }
})();
