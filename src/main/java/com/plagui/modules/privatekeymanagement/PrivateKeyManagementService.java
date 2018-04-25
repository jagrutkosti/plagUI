package com.plagui.modules.privatekeymanagement;

import multichain.command.*;
import multichain.command.builders.QueryBuilderGrant;
import multichain.object.KeyPair;
import multichain.object.SignRawTransactionOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle the private keys of the user depending on the choice of user from the following:
 * 0) Encrypt the private key using the user password
 * 1) Store the private key in the UI database
 * 2) The user handles their own private key
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
     * Sends zero valued transaction from main node address to created address for getting a UTXO (Unspent transaction
     * output) available for new address to start publishing data
     * @param blockchainAddress the blockchain address to which to send zero value transaction
     */
    public void sendZeroTransaction(String blockchainAddress) {
        try {
            System.out.println(WalletTransactionCommand.SendToAddress(blockchainAddress, 0));
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

        Map<String, Object> streamPayload = new HashMap<>();
        streamPayload.put("for", streamName);
        streamPayload.put("key", key);
        streamPayload.put("data", data);
        executeRawStreamCommands(blockchainAddress, streamPayload, privateKey, false);
    }

    /**
     * Send an asset between two blockchain addresses
     * @param fromAddress send from address
     * @param toAddress send to address
     * @param fromPrivateKey private key of fromAddress for authorizing transaction
     * @param amount the amount to send
     */
    public void sendCurrencyToPDServer(String fromAddress, String toAddress, String fromPrivateKey, int amount) {
        double amountInDouble = (double)amount/100000000;
        Map<String, Object> assetInfo = new HashMap<>();
        assetInfo.put("", amountInDouble);
        Map<String, Object> transferTo = new HashMap<>();
        transferTo.put(toAddress, assetInfo);
        executeRawStreamCommands(fromAddress, transferTo, fromPrivateKey, true);
    }

    public void executeRawStreamCommands(String fromAddress, Map<String, Object> payload, String privateKey, boolean isAsset) {
        String hexadecimalBlob;
        try {
            if(isAsset)
                hexadecimalBlob = RAWTransactionCommand.createRawSendFrom(fromAddress, payload, null);
            else
                hexadecimalBlob = RAWTransactionCommand.createRawSendFrom(fromAddress, null, payload);
            System.out.println(hexadecimalBlob);
            SignRawTransactionOut signedHexadecimal = RAWTransactionCommand.signRawTransactionWithPrivKey(hexadecimalBlob,privateKey);
            RAWTransactionCommand.sendRawTransaction(signedHexadecimal.getHex());
        } catch (MultichainException e) {
            e.printStackTrace();
        }

    }
}
