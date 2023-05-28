package com.exercise.phonebooking.service;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GSMArenaPhoneSpecsProvider {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String host = "https://www.gsmarena.com/";

    private Map<String, String> phoneSpecsPage = new HashMap<String, String>() {{
        put("Samsung Galaxy S9", "samsung_galaxy_s9-8966.php");
        put("Samsung Galaxy S8", "samsung_galaxy_s8-8161.php");
        put("Motorola Nexus 6", "motorola_nexus_6-6604.php");
        put("Oneplus 9", "oneplus_9-10747.php");
        put("Apple iPhone 13", "apple_iphone_13-11103.php");
        put("Apple iPhone 12", "apple_iphone_12-10509.php");
        put("Apple iPhone 11", "apple_iphone_11-9848.php");
        put("Apple iPhone X", "apple_iphone_x-8858.php");
        put("Nokia 3310", "nokia_3310-192.php");
    }};

    public Optional<PhoneSpecs> getPhoneSpecs(String phoneModel) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        try {
            webClient.getOptions().setJavaScriptEnabled(false);
            String uri = host + phoneSpecsPage.get(phoneModel);
            System.out.println("uri= " + uri);
            HtmlPage page = webClient.getPage(uri);
            HtmlElement specList = page.getElementById("specs-list")
                .getElementsByTagName("table").get(0)
                .getElementsByTagName("tbody").get(0);

            String tech = specList.getElementsByAttribute("a", "data-spec", "nettech")
                    .get(0).getTextContent();

            String net2G = specList.getElementsByAttribute("td", "data-spec", "net2g")
                    .get(0).getTextContent();

            List<HtmlElement> net3gElements = specList.getElementsByAttribute("td", "data-spec", "net3g");
            String net3G = net3gElements.isEmpty() ? "Not Available" : net3gElements.get(0).getTextContent();

            List<HtmlElement> net4gElements = specList.getElementsByAttribute("td", "data-spec", "net4g");
            String net4G = net4gElements.isEmpty() ? "Not Available" : net4gElements.get(0).getTextContent();

            logger.info(String.format("GSMArena says tech of %s is %s", phoneModel, tech));
            logger.info(String.format("GSMArena says 2G Band of %s is %s", phoneModel, net2G));
            logger.info(String.format("GSMArena says 3G Band of %s is %s", phoneModel, net3G));
            logger.info(String.format("GSMArena says 4G Band of %s is %s", phoneModel, net4G));

            return Optional.of(new PhoneSpecs(phoneModel, tech, net2G, Optional.ofNullable(net3G), Optional.ofNullable(net4G)));

        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}
