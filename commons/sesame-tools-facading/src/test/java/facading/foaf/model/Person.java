package facading.foaf.model;


import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.annotations.RDFFilter;
import at.newmedialab.sesame.facading.annotations.RDFType;
import at.newmedialab.sesame.facading.model.Facade;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import org.openrdf.model.URI;

import java.util.Set;

/**
 * Sample facade to describe a foaf:Person
 * <p/>
 * Author: Sebastian Schaffert
 */
@RDFType(Namespaces.NS_FOAF + "Person")
@RDFFilter(Namespaces.NS_FOAF + "Person")
public interface Person extends Facade {

    @RDF(Namespaces.NS_FOAF + "nick")
    public String getNick();
    public void setNick(String nick);

    /**
     * The  name of the user; mapped to the foaf:name RDF property
     */
    @RDF(Namespaces.NS_FOAF + "name")
    public String getName();
    public void setName(String firstName);


    @RDF(Namespaces.NS_FOAF + "mbox")
    public String getMbox();
    public void setMbox(String mbox);

    @RDF(Namespaces.NS_FOAF + "depiction")
    public URI getDepiciton();
    public void setDepiction(URI depiction);

    @RDF(Namespaces.NS_FOAF + "account")
    public Set<OnlineAccount> getOnlineAccounts();
    public void setOnlineAccounts(Set<OnlineAccount> onlineAccounts);

    @RDF(Namespaces.NS_FOAF + "knows")
    public Set<Person> getFriends();
    public void setFriends(Set<Person> friends);
    public boolean hasFriends();
}
