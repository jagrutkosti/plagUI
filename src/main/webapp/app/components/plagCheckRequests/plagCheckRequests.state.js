/**
 * Created by Jagrut on 04-07-2017.
 */
(function() {
    "use strict";

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig ($stateProvider) {
        $stateProvider
            .state('plagCheckRequests', {
                parent: 'app',
                url: '/plagCheckRequests',
                data: {
                    authorities: ['ROLE_USER'],
                    pageTitle: 'Your Requests'
                },
                views: {
                    'content@': {
                        templateUrl: 'app/components/plagCheckRequests/plagCheckRequests.html',
                        controller: 'PlagCheckRequestsController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    allRequests: ['PlagCheckRequestsService', function(PlagCheckRequestsService) {
                        return PlagCheckRequestsService.getLoggedInUserRequests().then(function(response) {
                            return response;
                        })
                    }]
                }
            });
    }
})();
