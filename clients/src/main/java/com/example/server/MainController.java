package com.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.flows.BankApproveFlow;
import com.template.flows.InitiatePropertyFlow;
import com.template.flows.SurveyorApproveFlow;
import com.template.flows.TransferPropertyFlow;
import com.template.schema.PropertySchemaV1;
import com.template.states.PropertyState;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.apache.activemq.artemis.core.server.group.impl.Response;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/land") // The paths for HTTP requests are relative to this base path.
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public MainController(NodeRPCConnection rpc) {
        this.proxy = rpc.getProxy();
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /** Helpers for filtering the network map cache. */
    public String toDisplayString(X500Name name){
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo){
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses() {
        return proxy.nodeInfo().getAddresses().toString();
    }

    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion() {
        return Integer.toString(proxy.nodeInfo().getPlatformVersion());
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states() {
        return proxy.vaultQuery(PropertyState.class).getStates().toString();
    }

    @GetMapping(value = "/me",produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami(){
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @GetMapping(value = "/currentypropertystate",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<PropertyState>> getCurremtProperty() {
        return proxy.vaultQuery(PropertyState.class).getStates();
    }

    @GetMapping(value = "/filterpropertybyId",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<PropertyState>> getCurrentPropertybyID
            (@RequestParam(value = "propertyId") String propertyId) {

        UniqueIdentifier linearid = UniqueIdentifier.Companion.fromString(propertyId);
        List<UniqueIdentifier> stateid = new ArrayList<>();
        stateid.add(linearid);

        QueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria
                (null, stateid, Vault.StateStatus.ALL, null);

        return  proxy.vaultQueryByCriteria(criteria, PropertyState.class).getStates();
    }

    @GetMapping(value = "/filterpropertybyPrice",produces = APPLICATION_JSON_VALUE)
    public List<StateAndRef<PropertyState>> getpropertybyPrice
            (@RequestParam(value = "propertyPrice") int price) throws NoSuchFieldException {

        QueryCriteria criteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
        Field propertyPrice = PropertySchemaV1.PersistentProperty.class.getDeclaredField("propertyPrice");
        CriteriaExpression statusCriteriaexpression = Builder.lessThanOrEqual(propertyPrice, price);

        QueryCriteria statusCriteria = new QueryCriteria.VaultCustomQueryCriteria<>(statusCriteriaexpression);

        QueryCriteria newstatusCriteria = criteria.and(statusCriteria);

        return proxy.vaultQueryByCriteria(newstatusCriteria, PropertyState.class).getStates();
    }

    @PostMapping (value = "/transfer", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> transferProperty
            (@RequestParam(value = "propertyId") String propertyId,
             @RequestParam(value = "party") String party) {

        UniqueIdentifier linearId = new UniqueIdentifier(null,UUID.fromString(propertyId));
        Party newOwner = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(party));
        try {
            proxy.startTrackedFlowDynamic(TransferPropertyFlow.TransferInitiator.class, linearId, newOwner).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("Property ID: "+linearId.toString()+" transferred to "+party+".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping (value = "/surveyapprove", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> surveyorapprove
            (@RequestParam(value = "propertyId") String propertyId,
             @RequestParam(value = "party") String party) {

        UniqueIdentifier linearId = new UniqueIdentifier(null,UUID.fromString(propertyId));
        Party newOwner = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(party));
        try {
            proxy.startTrackedFlowDynamic(SurveyorApproveFlow.SurveyorApproveInitiate.class, linearId, newOwner).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("Property ID: "+linearId.toString()+" is approved by Surveyor and transferred to "+party+".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping (value = "/bankapprove", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> bankapprove
            (@RequestParam(value = "propertyId") String propertyId,
             @RequestParam(value = "party") String party) {

        UniqueIdentifier linearId = new UniqueIdentifier(null,UUID.fromString(propertyId));
        Party newOwner = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(party));
        try {
            proxy.startTrackedFlowDynamic(BankApproveFlow.BankApproveInitiate.class, linearId, newOwner).getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED).body("Property ID: "+linearId.toString()+" is approved by Bank and  transferred to "+party+".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping (value = "/createproperty" , produces =  APPLICATION_JSON_VALUE)
    public ResponseEntity<String> issueProperty(@RequestParam(value = "propertyId") int propertyId,
                                                @RequestParam(value = "propertyAddress") String propertyAddress,
                                                @RequestParam(value = "propertyPrice") int propertyPrice,
                                                @RequestParam(value = "buyerId") int buyerId,
                                                @RequestParam(value = "sellerId") int sellerId
                                                ) throws IllegalArgumentException {

        // Get party objects for myself.
        Party party = proxy.nodeInfo().getLegalIdentities().get(0);
        System.out.println("Party Name: " + party);
        try {
            // Start the InitiatePropertyFlow. We block and waits for the flow to return.
            PropertyState state = new PropertyState(propertyId,propertyAddress,propertyPrice,buyerId,sellerId,false,false,party,"New Property",party);
            SignedTransaction result = proxy.startTrackedFlowDynamic(InitiatePropertyFlow.class,propertyId,propertyAddress,propertyPrice,buyerId,sellerId).getReturnValue().get();
            // Return the response.
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Transaction id "+ result.getId() +" committed to ledger.\n " + result.getTx().getOutput(0));
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}