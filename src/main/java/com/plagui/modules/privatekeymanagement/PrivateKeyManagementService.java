package com.plagui.modules.privatekeymanagement;

import multichain.command.AddressCommand;
import multichain.command.GrantCommand;
import multichain.command.MultichainException;
import multichain.command.RAWTransactionCommand;
import multichain.command.builders.QueryBuilderGrant;
import multichain.object.KeyPair;
import multichain.object.SignRawTransactionOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class to handle the private keys of the user depending on the choice of user from the following:
 * 1) Store the private key in the UI database
 * 2) Encrypt the private key using the user password
 * 3) The user handles their own private key
 */
@Service
public class PrivateKeyManagementService {

    private final Logger log = LoggerFactory.getLogger(PrivateKeyManagementService.class);

    /**
     * This method will generate a blockchain address with corresponding public and private key.
     * The address is not yet added in the blockchain
     * @return KeyPair object containing populated fields
     */
    public KeyPair generateKeyPair() {
        log.info("PrivateKeyManagementService::generateKeyPair()");
        try {
            return AddressCommand.createKeyPairs();
        } catch (MultichainException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds the created blockchain address to a node
     * @param blockchainAddress
     */
    public void importAddress(String blockchainAddress) {
        log.info("PrivateKeyManagementService::importAddress()");
        try {
            AddressCommand.importAddress(blockchainAddress, "", false);
        } catch (MultichainException e) {
            e.printStackTrace();
        }
    }

    /**
     * Grants receive, send and connect permission to the newly created user
     * @param blockchainAddress the address of the user which needs to be granted the permission
     */
    public void grantReceiveSendConnectPermission(String blockchainAddress) {
        log.info("PrivateKeyManagementService::grantReceiveSendPermission()");
        try {
            GrantCommand.grant(blockchainAddress, GrantCommand.CONNECT);
            GrantCommand.grant(blockchainAddress, GrantCommand.SEND);
            GrantCommand.grant(blockchainAddress, GrantCommand.RECEIVE);
        } catch (MultichainException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publish an item to a stream using the blockchain address and runtime fetched private key.
     * @param blockchainAddress the blockchain address who wants to publish an item
     * @param privateKey the private key of the user
     * @param streamName the name of the stream to publish to
     * @param key the key for the particular stream item
     * @param data the data that needs to be published
     */
    public void publishStreamItemForPrivateKey(String blockchainAddress, String privateKey, String streamName, String key, String data) {
        log.info("PrivateKeyManagementService::publishStreamItemForPrivateKey()");

        String streamPayload = "'[{\"for\":\""+ streamName +"\",\"key\":\""+ key +"\", \"data\":\""+ data +"\"}]'";
        try {
            String hexadecimalBlob = RAWTransactionCommand.createRawSendFrom(blockchainAddress, null, streamPayload);
            SignRawTransactionOut signedHexadecimal = RAWTransactionCommand.signRawTransactionWithPrivKey(hexadecimalBlob,privateKey);
            RAWTransactionCommand.sendRawTransaction(signedHexadecimal.getHex());
        } catch (MultichainException e) {
            e.printStackTrace();
        }
    }
}
