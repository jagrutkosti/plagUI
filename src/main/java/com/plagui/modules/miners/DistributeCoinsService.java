package com.plagui.modules.miners;

import com.plagui.config.Constants;
import multichain.command.*;
import multichain.object.MultiBalance;
import multichain.object.Permission;
import multichain.object.StreamItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class should be run on each individual miner's computer system to distribute the mined currency amongst the
 * associated users.
 */
@Component
public class DistributeCoinsService {
    private final Logger log = LoggerFactory.getLogger(DistributeCoinsService.class);

    private String associationInfoFromStream;
    private int partOfMinedCurrencyForMiner;

    public DistributeCoinsService() {
        this.associationInfoFromStream = Constants.MINERS_USERS_ASSOCIATION_STREAM;
        this.partOfMinedCurrencyForMiner = 3;
    }

    public DistributeCoinsService(String streamName, int partOfMinedCurrencyForMiner) {
        this.associationInfoFromStream = streamName;
        this.partOfMinedCurrencyForMiner = partOfMinedCurrencyForMiner;
    }

    @Scheduled(fixedRate = 86400000)
    public void distributeMinedCoins() {
        log.info("DistributeCoinsService # distributeMinedCoins");
        List<MinerBalance> allMinersWithBalances = getMiningAddressesAndCoinQty();
        for(MinerBalance minerInfo : allMinersWithBalances) {
            if(minerInfo.getQty() > 0.0001) {
                List<String> associatedUsers = getAllUsersAssociatedWithMiner(minerInfo.getAddress());
                double qty = minerInfo.getQty()/(associatedUsers.size() + partOfMinedCurrencyForMiner);
                for(String userAddress : associatedUsers) {
                    transferCoins(minerInfo.getAddress(), userAddress, qty);
                }
            }
        }
    }

    /**
     * Get all addresses from the wallet that have mine permission. Then fetch balances for those addresses.
     * @return {List} of MinerBalance containing addresses that have mine permission and their balance.
     */
    public List<MinerBalance> getMiningAddressesAndCoinQty() {
        log.info("DistributeCoinsService # getMiningAddressesAndCoinQty()");
        List<MinerBalance> mineAddressAndCoinQty = new ArrayList<>();
        List<String> walletAddressWithMinePermission = new ArrayList<>();

        List<Permission> minerAddressesObject = null;
        List<MultiBalance> allWalletAddressBalances = null;
        try {
            minerAddressesObject = GrantCommand.listPermissionsList((byte)0b0010000);
            allWalletAddressBalances = AddressCommand.getMultiBalances();
        } catch (MultichainException e) {
            e.printStackTrace();
        }
        for(Permission address: minerAddressesObject) {
            walletAddressWithMinePermission.add(address.getAddress());
        }
        for(MultiBalance balance : allWalletAddressBalances) {
            if(walletAddressWithMinePermission.contains(balance.getLabel())) {
                MinerBalance minerWithBalance = new MinerBalance();
                minerWithBalance.setAddress(balance.getLabel());
                minerWithBalance.setQty(balance.getFirstAsset().getQty());
                mineAddressAndCoinQty.add(minerWithBalance);
            }
        }
        return mineAddressAndCoinQty;
    }

    /**
     * Get all associated users with the provided miner address from the default stream initialized in this class.
     * @param minerAddress the miner address
     * @return {List} of addresses of users associated with given miner address
     */
    public List<String> getAllUsersAssociatedWithMiner(String minerAddress) {
        log.info("DistributeCoinsService # getAllUsersAssociatedWithMiners()");
        List<String> allAssociatedUsers = new ArrayList<>();
        List<StreamItem> allAssociatedUsersObject = null;
        try {
            allAssociatedUsersObject = StreamCommand.listStreamKeyItems(associationInfoFromStream, minerAddress);
            if (allAssociatedUsersObject != null) {
                for(StreamItem item : allAssociatedUsersObject) {
                    allAssociatedUsers.add(new String(DatatypeConverter.parseHexBinary(item.getData()), "UTF-8"));
                }
            }
        } catch (MultichainException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return allAssociatedUsers;
    }

    /**
     * Transfers coins from the miner address to the user address
     * @param minerAddress the address of the miner
     * @param userAddress the address of the user
     * @param qty the amount of coins to transfer
     * @return {String} transaction id or null in case of exception
     */
    public String transferCoins(String minerAddress, String userAddress, double qty) {
        log.info("DistributeCoinsService # transferCoins()");
        try {
            return IssueCommand.sendFrom(minerAddress, userAddress, String.format("%.8f", qty));
        } catch (MultichainException e) {
            e.printStackTrace();
            return null;
        }
    }
}
