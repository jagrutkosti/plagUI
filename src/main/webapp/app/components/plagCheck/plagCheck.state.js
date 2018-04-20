/**
 * Created by Jagrut on 21-06-2017.
 */
(function() {
    "use strict";

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig ($stateProvider) {
        $stateProvider
            .state('plagCheck', {
                parent: 'app',
                url: '/plagCheck',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Check for Plagiarism'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/components/plagCheck/plagCheck.html',
                        controller: 'PlagCheckController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    currentUserWalletAddress : ['Principal', function(Principal) {
                        return Principal.identity().then(function(user) {
                            return user.walletAddress;
                        });
                    }],
                    pdServers: ['UploadDocsService', function(UploadDocsService) {
                        return UploadDocsService.getPdServersList();
                    }]
                }
            });
    }
})();
