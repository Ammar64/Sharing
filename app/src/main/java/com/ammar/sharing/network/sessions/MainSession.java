package com.ammar.sharing.network.sessions;

import com.ammar.sharing.common.utils.Utils;
import com.ammar.sharing.models.User;
import com.ammar.sharing.network.Request;
import com.ammar.sharing.network.Response;



// Don't add to the server
public class MainSession extends HTTPSession {
    public MainSession(User user) {
        super(user);
    }


}
