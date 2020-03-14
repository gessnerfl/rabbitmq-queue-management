package de.gessnerfl.rabbitmq.queue.management.service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "de.gessnerfl.security.authentication")
public class AuthenticationConfig {

    private boolean enabled = false;
    private final LdapAuthenticationConfiguration ldap = new LdapAuthenticationConfiguration();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LdapAuthenticationConfiguration getLdap() {
        return ldap;
    }

    public static class LdapAuthenticationConfiguration {
        private String groupRoleAttribute = "cn";
        private String groupSearchBase = "";
        private String groupSearchFilter = "(uniqueMember={0})";
        private String userSearchBase = "";
        private String userSearchFilter = "uid={0}";
        private List<String> userDnPatterns = new ArrayList<>();
        private final LdapContextSource contextSource = new LdapContextSource();

        public String getGroupRoleAttribute() {
            return groupRoleAttribute;
        }

        public void setGroupRoleAttribute(String groupRoleAttribute) {
            this.groupRoleAttribute = groupRoleAttribute;
        }

        public String getGroupSearchBase() {
            return groupSearchBase;
        }

        public void setGroupSearchBase(String groupSearchBase) {
            this.groupSearchBase = groupSearchBase;
        }

        public String getGroupSearchFilter() {
            return groupSearchFilter;
        }

        public void setGroupSearchFilter(String groupSearchFilter) {
            this.groupSearchFilter = groupSearchFilter;
        }

        public String getUserSearchBase() {
            return userSearchBase;
        }

        public void setUserSearchBase(String userSearchBase) {
            this.userSearchBase = userSearchBase;
        }

        public String getUserSearchFilter() {
            return userSearchFilter;
        }

        public void setUserSearchFilter(String userSearchFilter) {
            this.userSearchFilter = userSearchFilter;
        }

        public List<String> getUserDnPatterns() {
            return userDnPatterns;
        }

        public void setUserDnPatterns(List<String> userDnPatterns) {
            this.userDnPatterns = userDnPatterns;
        }

        public LdapContextSource getContextSource() {
            return contextSource;
        }

    }

    public static class LdapContextSource {
        private String managerPassword;
        private String managerDn;
        private Integer port = 389;
        private String root;
        private String url;

        public String getManagerPassword() {
            return managerPassword;
        }

        public void setManagerPassword(String managerPassword) {
            this.managerPassword = managerPassword;
        }

        public String getManagerDn() {
            return managerDn;
        }

        public void setManagerDn(String managerDn) {
            this.managerDn = managerDn;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
