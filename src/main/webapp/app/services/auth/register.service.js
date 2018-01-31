(function () {
    'use strict';

    angular
        .module('plagUiApp')
        .factory('Register', Register);

    Register.$inject = ['$resource'];

    function Register ($resource) {
        return $resource('api/register', {}, {
            getAllActiveMiners: {
                method: 'GET',
                isArray: true,
                url: 'api/getAllActiveMiners'
            }
        });
    }
})();
