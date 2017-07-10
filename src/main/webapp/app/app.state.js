(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('app', {
            abstract: true,
            views: {
                'navbar@': {
                    templateUrl: 'app/layouts/navbar/navbar.html',
                    controller: 'NavbarController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                authorize: ['Auth',
                    function (Auth) {
                        return Auth.authorize();
                    }
                ],
                numOfPendingRequests : ['PlagCheckRequestsService', function(PlagCheckRequestsService) {
                    return PlagCheckRequestsService.getNumberOfPendingRequests();
                }],
                streamPermissions : ['PermissionsService', function(PermissionsService) {
                    return PermissionsService.getPermissionsForUser().then(function(response) {
                        return response;
                    });
                }]
            }
        });
    }
})();
