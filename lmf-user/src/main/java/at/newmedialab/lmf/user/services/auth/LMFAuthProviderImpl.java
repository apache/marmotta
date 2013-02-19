/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.newmedialab.lmf.user.services.auth;

import at.newmedialab.lmf.user.api.AccountService;
import at.newmedialab.lmf.user.api.AuthenticationProvider;
import at.newmedialab.lmf.user.model.UserAccount;
import at.newmedialab.lmf.user.services.AuthenticationServiceImpl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
@Named(AuthenticationServiceImpl.DEFAULT_AUTH_PROVIDER_NAMED)
@Default
public class LMFAuthProviderImpl implements AuthenticationProvider {

    @Inject
    private AccountService accountService;

    @Override
    public boolean checkPassword(UserAccount login, String passwd) {
        return accountService.checkPassword(login, passwd);
    }

    @Override
    public boolean updatePassword(UserAccount login, String newPasswd) {
        accountService.setPassword(login, newPasswd);
        return true;
    }

}
