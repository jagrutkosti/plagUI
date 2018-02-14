(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider.state('register', {
            parent: 'account',
            url: '/register',
            data: {
                authorities: [],
                pageTitle: 'Registration'
            },
            views: {
                'content@': {
                    templateUrl: 'app/account/register/register.html',
                    controller: 'RegisterController',
                    controllerAs: 'vm'
                }
            },
            resolve : {
                listOfMiners: ['Register', function(Register) {
                    return Register.getAllActiveMiners();
                }],
                plagchainaddress: ['Register', function (Register) {
                    return Register.generatePlagchainAddress();
                }]
            }
        });
    }
})();
