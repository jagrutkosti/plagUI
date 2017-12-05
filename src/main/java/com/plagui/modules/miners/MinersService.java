package com.plagui.modules.miners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plagui.config.Constants;
import com.plagui.modules.UtilService;
import multichain.command.GrantCommand;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.Permission;
import multichain.object.StreamItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MinersService {
    private final Logger log = LoggerFactory.getLogger(MinersService.class);
    private final UtilService utilService;

    public MinersService(UtilService utilService) {
        this.utilService = utilService;
    }

    /**
     * Adds the given miner into the list of miners stream. The miner should be approved before.
     * Approve is not handled in this method.
     * @param newApprovedMiner the miner object to be added into the stream
     */
    public void addMinerToStream(MinersDTO newApprovedMiner) {
        Gson gson = new GsonBuilder().create();
        String data = gson.toJson(newApprovedMiner);
        data = DatatypeConverter.printHexBinary(data.getBytes());
        utilService.submitToPlagchain(Constants.MINERS_STREAM, newApprovedMiner.getMinerAddress(), data);
    }

    /**
     * Adds an association to the MINERS_USERS_ASSOCIATION_STREAM with key being miner address and data is simply the
     * user address. This information will be public and used to distribute funds from the miners.
     * @param minerAddress the address of the miner
     * @param userAddress the address of the user
     */
    public void addMinerAndUserAssociationToPlagchain(String minerAddress, String userAddress) {
        log.info("MinersService#addMinerAndUserAssociationToPlagchain() called.");
        try {
            StreamCommand.publishStream(Constants.MINERS_USERS_ASSOCIATION_STREAM, minerAddress, DatatypeConverter.printHexBinary(userAddress.getBytes()));
        } catch (MultichainException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches all minersInfo that are stored in the Contants.MINERS_STREAM
     * @return {List} of all stored miners in blockchain
     */
    public List<MinersDTO> getAllAvailableMinersInfo() {
        log.info("MinersService#getAllAvailableMinersInfo() called.");
        List<MinersDTO> allMinersInfo = new ArrayList<>();
        try {
            List<StreamItem> allMinersInStream = StreamCommand.listStreamItems(Constants.MINERS_STREAM);
            for(StreamItem item : allMinersInStream) {
                MinersDTO minerInfo = new MinersDTO();
                JSONObject itemData = utilService.transformDataFromHextoJSON(item.getData());
                if(itemData != null) {
                    minerInfo.setMinerAddress(itemData.getString("minerAddress"));
                    minerInfo.setMinerName(itemData.getString("minerName"));
                    minerInfo.setEmailFormats(Arrays.asList(itemData.getString("emailFormats").split(",")));
                    allMinersInfo.add(minerInfo);
                }
            }
        } catch (MultichainException | JSONException e) {
            e.printStackTrace();
        }
        return allMinersInfo;
    }

    /**
     * Remove the miners that are currently not active and return the remaining list of miners.
     * The listPermissionsList command takes in byte as parameter for permission. 0b0010000 means 'mine' permission.
     * @param minersInfoFromStream the list that needs to be checked for inactive miners
     * @return {List} of miners that are currently active
     */
    public List<MinersDTO> removeInactiveMiners(List<MinersDTO> minersInfoFromStream) {
        log.info("MinersService#removeAllInactiveMiners() called.");
        try {
            List<Permission> addressesWithMinePermission = GrantCommand.listPermissionsList((byte)0b0010000);
            List<String> activeMinerAddress = new ArrayList<>();
            for(Permission address: addressesWithMinePermission) {
                activeMinerAddress.add(address.getAddress());
            }
            minersInfoFromStream.removeIf(minersDTO -> !activeMinerAddress.contains(minersDTO.getMinerAddress()));
        } catch (MultichainException e) {
            e.printStackTrace();
        }
        return minersInfoFromStream;
    }
}
