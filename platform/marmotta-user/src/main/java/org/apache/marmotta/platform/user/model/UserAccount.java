/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.platform.user.model;

import org.apache.marmotta.commons.util.HashUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Stephanie Stroka
 * Date: 18.05.2011
 * Time: 11:29:17
 */

public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Enum of avalable password-hash algorithms.
     *
     * @author Jakob Frank <jakob.frank@salzburgresearch.at>
     *
     */
    public enum PasswordHash {
        PLAIN {
            @Override
            protected String hash(String in) {
                return in;
            }
        },
        MD5 {
            @Override
            protected String hash(String in) {
                return HashUtils.md5sum(in);
            }
        },
        SHA1 {
            @Override
            protected String hash(String in) {
                return HashUtils.sha1(in);
            }
        };

        public String encrypt(String passwd) {
            return passwd == null ? null : ":" + this.toString().toLowerCase() + "::" + this.hash(passwd);
        }

        protected abstract String hash(String in);

        private static final Pattern P = Pattern.compile(":(\\w+)::(.*)");

        public static boolean checkPasswd(String encrypted, String passwd) {
            if (encrypted != null && passwd != null) {
                try {
                    Matcher m = P.matcher(encrypted);
                    if (m.matches()) {
                        final PasswordHash h = PasswordHash.valueOf(m.group(1).toUpperCase());
                        return encrypted.matches(h.encrypt(passwd));
                    }
                } catch (Exception e) {
                }
            }
            return false;
        }

        public static PasswordHash getPasswordHash(String passwdHash) {
            if (passwdHash != null) {
                try {
                    Matcher m = P.matcher(passwdHash);
                    if (m.matches()) {
                        final PasswordHash h = PasswordHash.valueOf(m.group(1).toUpperCase());
                        return h;
                    }
                } catch (Exception e) {
                }
            }
            return SHA1;
        }
    }


    /* the user's credentials */
    private String login;
    private String  passwdHash;

    /* the user's webId that points to their RDF user profile */
    private String webId;

    private Set<String>     roles;

    public UserAccount() {
        roles = new HashSet<String>();
    }

    public UserAccount(String login, String webId) {
        this();
        this.login = login;
        this.webId = webId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswdHash() {
        return passwdHash;
    }

    public void setPasswdHash(String passwdHash) {
        this.passwdHash = passwdHash;
    }

    public boolean checkPasswd(String password) {
        return PasswordHash.checkPasswd(getPasswdHash(), password);
    }

    public void setPasswd(String passwd) {
        this.setPasswd(PasswordHash.SHA1, passwd);
    }

    public void setPasswd(PasswordHash alg, String passwd) {
        this.passwdHash = alg.encrypt(passwd);
    }

    public String getWebId() {
        return webId;
    }

    public void setWebId(String webId) {
        this.webId = webId;
    }


    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAccount that = (UserAccount) o;

        if (login != null ? !login.equals(that.login) : that.login != null) return false;
        if (webId != null ? !webId.equals(that.webId) : that.webId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (webId != null ? webId.hashCode() : 0);
        return result;
    }

}
