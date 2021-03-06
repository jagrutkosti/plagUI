/**
 * Created by Jagrut on 07-06-2017.
 */
(function() {
    "use strict";

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig ($stateProvider) {
       $stateProvider
           .state('uploadDocs', {
               parent: 'app',
               url: '/uploadDocs',
               data: {
                   authorities: ['ROLE_USER'],
                   pageTitle: 'Upload Documents'
               },
               views: {
                   'content@': {
                       templateUrl: 'app/components/uploadDocs/uploadDocs.html',
                       controller: 'UploadDocsController',
                       controllerAs: 'vm'
                   }
               },
               resolve: {
                   pdServers: ['UploadDocsService', function(UploadDocsService) {
                       return UploadDocsService.getPdServersList();
                   }]
               }
           });
    }
})();
