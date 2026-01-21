package mph.trunksku.apps.myssh.model;

public class ServerModel {
    private int id;
    private String name;
    private String host;
    private int port;
    private int sshPort;
    private int sslPort;
    private int ovpnPort;
    private String country;
    private String countryCode;
    private String flagUrl;
    private boolean isPremium;
    private String sshUsername;
    private String sshPassword;
    private String sni;
    private String payload;
    private String proxyHost;
    private int proxyPort;
    
    public ServerModel() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public int getSshPort() { return sshPort; }
    public void setSshPort(int sshPort) { this.sshPort = sshPort; }
    
    public int getSslPort() { return sslPort; }
    public void setSslPort(int sslPort) { this.sslPort = sslPort; }
    
    public int getOvpnPort() { return ovpnPort; }
    public void setOvpnPort(int ovpnPort) { this.ovpnPort = ovpnPort; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    
    public String getFlagUrl() { return flagUrl; }
    public void setFlagUrl(String flagUrl) { this.flagUrl = flagUrl; }
    
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
    
    public String getSshUsername() { return sshUsername; }
    public void setSshUsername(String sshUsername) { this.sshUsername = sshUsername; }
    
    public String getSshPassword() { return sshPassword; }
    public void setSshPassword(String sshPassword) { this.sshPassword = sshPassword; }
    
    public String getSni() { return sni; }
    public void setSni(String sni) { this.sni = sni; }
    
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    
    public String getProxyHost() { return proxyHost; }
    public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }
    
    public int getProxyPort() { return proxyPort; }
    public void setProxyPort(int proxyPort) { this.proxyPort = proxyPort; }
    
    @Override
    public String toString() {
        return name + " (" + country + ")";
    }
}
