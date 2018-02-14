(function() {
    'use strict';

    angular
        .module('plagUiApp')
        .controller('RegisterController', RegisterController);


    RegisterController.$inject = [ '$timeout', 'Auth', 'LoginService', 'listOfMiners', 'plagchainaddress'];

    function RegisterController ($timeout, Auth, LoginService, listOfMiners, plagchainaddress) {
        var vm = this;
        vm.listOfMiners = listOfMiners;
        vm.plagchainaddress = plagchainaddress;
        vm.doNotMatch = null;
        vm.error = null;
        vm.errorUserExists = null;
        vm.login = LoginService.open;
        vm.register = register;
        vm.registerAccount = {
            privKeyOption: '0'
        };
        vm.success = null;
        vm.invalidEmail = true;
        vm.checkEmailFormat = checkEmailFormat;

        $timeout(function (){angular.element('#login').focus();});

        function register () {
            if (vm.registerAccount.password !== vm.confirmPassword) {
                vm.doNotMatch = 'ERROR';
            } else {
                vm.registerAccount.langKey =  'en' ;
                vm.doNotMatch = null;
                vm.error = null;
                vm.errorUserExists = null;
                vm.errorEmailExists = null;

                vm.registerAccount.plagchainAddress = vm.plagchainaddress.address;
                vm.registerAccount.plagchainPubkey = vm.plagchainaddress.pubkey;

                if(vm.registerAccount.privKeyOption === '0')
                    vm.registerAccount.plagchainPrivkey = CryptoJS.AES.encrypt(vm.plagchainaddress.privkey, vm.registerAccount.password).toString();
                else if(vm.registerAccount.privKeyOption === '1')
                    vm.registerAccount.plagchainPrivkey = vm.plagchainaddress.privkey;

                Auth.createAccount(vm.registerAccount).then(function () {
                    vm.success = 'OK';
                }).catch(function (response) {
                    vm.success = null;
                    if (response.status === 400 && response.data === 'login already in use') {
                        vm.errorUserExists = 'ERROR';
                    } else if (response.status === 400 && response.data === 'email address already in use') {
                        vm.errorEmailExists = 'ERROR';
                    } else {
                        vm.error = 'ERROR';
                    }
                });
            }
        }

        /**
         * Check if the entered email ends with at least one format required by the selected miner
         */
        function checkEmailFormat () {
            var count = 0;
            vm.registerAccount.selectedMiner.emailFormats.forEach(function(format) {
                if(endsWith(vm.registerAccount.email, format)) {
                    vm.invalidEmail = false;
                    count++;
                }
            });
            if(count === 0)
                vm.invalidEmail = true;
            function endsWith(str, word) {
                return str.indexOf(word, str.length - word.length) !== -1;
            }
        }
    }
})();
