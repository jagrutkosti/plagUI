/**
 * Created by Jagrut on 27-06-2017.
 */
(function() {
    "use strict";

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig ($stateProvider) {
       $stateProvider
           .state('showDocDetails', {
               parent: 'app',
               url: '/showDocDetails',
               data: {
                   authorities: ['ROLE_USER'],
                   pageTitle: 'Your Documents'
               },
               views: {
                   'content@': {
                       templateUrl: 'app/components/showDocDetails/showDocDetails.html',
                       controller: 'DocDetailsController',
                       controllerAs: 'vm'
                   }
               },
               resolve: {
                   docDetails : ['DocDetailsService', function(DocDetailsService) {
                       return DocDetailsService.getDocDetails().then(function(response) {
                           return response;
                       });
                   }]
               }
           });
    }
})();
