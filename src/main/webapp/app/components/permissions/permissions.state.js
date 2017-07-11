/**
 * Created by Jagrut on 11-07-2017.
 */
(function() {
    "use strict";

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig ($stateProvider) {
        $stateProvider
            .state('permissionRequests', {
                parent: 'app',
                url: '/permissionRequests',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Permissions'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/components/permissions/permissions.html',
                        controller: 'PermissionsController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    currentUserWalletAddress : ['Principal', function(Principal) {
                        return Principal.identity().then(function(user) {
                            return user.walletAddress;
                        });
                    }]
                }
            });
    }
})();
