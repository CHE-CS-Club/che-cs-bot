package club.checs.csbot.managers;

import com.google.firebase.database.*;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;

public class RoleManager {
    private IDiscordClient client;

    public RoleManager(IDiscordClient client) {
        this.client = client;
        // See if any verify were sent when the bot was offline
        new Thread(() -> { // Bot isn't immediately ready when this is constructed. Wait for it to be ready async.
            while (!client.isReady())  // Wait for client to be ready
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            for (IGuild guild : client.getGuilds()) {
                for (IChannel channel : guild.getChannels()) {
                    if (channel.getName().equalsIgnoreCase("verify")) {
                        MessageHistory history = channel.getFullMessageHistory();
                        for (int i = history.size() - 2; i >= 0; i--) {
                            processVerifyMessage(history.get(i)); // Hopefully doesn't affect the MessageHistory when it deletes
                        }
                    }
                }
            }
        }).start();
    }

    @EventSubscriber
    public void onUserReact(ReactionEvent e) {
        // TODO Somehow allow servers to create rules channels. For now, this one is che cs specific
        if (!e.getChannel().getName().equalsIgnoreCase("rules")) // #rules
            return;
        // Make sure the user actually added a reaction and that it was the correct one
        // TODO Check if the right emoji
        if (!e.getReaction().getUserReacted(e.getUser()))
            return;
        DatabaseReference userRef = getUserRef(e.getGuild()).child(e.getUser().getStringID());
        userRef.child("accepted_rules").setValue(true);
    }

    @EventSubscriber
    public void onMessageSent(MessageEvent e) {
        if (e instanceof MessageDeleteEvent)
            return;
        // TODO Somehow allow servers to create verify channels. For now, this one is che cs specific
        if (!e.getChannel().getName().equalsIgnoreCase("verify")) // #verify
            return;

        processVerifyMessage(e.getMessage());
    }

    private void processVerifyMessage(final IMessage message) {
        // Make sure the message given is valid
        String messageS = message.getContent();
        String[] split = messageS.split("/");
        if (split.length != 3) {
            RequestBuffer.request(() -> message.getAuthor().getOrCreatePMChannel().sendMessage("Uh oh, looks " +
                    "like your verfiy message didn't follow our format. To verify please follow the proper format. " +
                    "If you're having issues please direct message one of our officers."));
            message.delete();
            return;
        }
        String firstName = split[0].trim();
        String lastName = split[1].trim();
        String email = split[2].trim();

        final IGuild guild = message.getGuild();

        // Try and set the users information
        DatabaseReference userRef = getUserRef(guild).child(message.getAuthor().getStringID());
        userRef.child("accepted_rules").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    // If database doesn't say accepted
                    if (snapshot.getValue() == null || !(boolean) snapshot.getValue()) {
                        // If the user didn't react with the white_check_mark in rules
                        boolean foundAccepted = false;
                        for (IChannel channel : guild.getChannels()) {
                            if (channel.getName().equalsIgnoreCase("rules")) {
                                if (channel.getFullMessageHistory().get(0)
                                        .getReactions().get(0)
                                        .getUserReacted(message.getAuthor()))
                                    foundAccepted = true;
                            }
                        }

                        if (!foundAccepted) {
                            // They didn't accept rules. Tell them
                            RequestBuffer.request(() -> message.getAuthor().getOrCreatePMChannel().sendMessage("Uh oh, " +
                                    "looks like you forgot to accept the rules. Go to #rules and click the " +
                                    ":white_check_mark: to agree to them. After you accept try to verify again."));
                            message.delete();
                            return;
                        } else { // If they did, means we don't have them in the db. Add them as accepted
                            DatabaseReference userRef = getUserRef(guild).child(message.getAuthor().getStringID());
                            userRef.child("accepted_rules").setValue(true);
                        }
                    }

                    // Upload data
                    userRef.child("firstName").setValue(firstName);
                    userRef.child("lastName").setValue(lastName);
                    userRef.child("email").setValue(email);

                    // Set the nickname
                    guild.setUserNickname(message.getAuthor(), firstName);

                    List<IRole> existing = guild.getRolesForUser(message.getAuthor());
                    existing.add(guild.getRolesByName("Members").get(0));
                    IRole[] roles = new IRole[existing.size()];
                    for (int i = 0; i < existing.size(); i++)
                        roles[i] = existing.get(i);

                    guild.editUserRoles(message.getAuthor(), roles);
                    message.delete();
                } catch (
                        Exception e)

                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                RequestBuffer.request(() -> message.getAuthor().getOrCreatePMChannel().sendMessage("Uh oh, looks " +
                        "like our auto verifier failed to verify you. Please try again or direct message one of " +
                        "our officers if you're having issues."));
                error.toException().printStackTrace();
                message.delete();
            }
        });
    }

    private DatabaseReference getUserRef(IGuild guild) {
        return FirebaseDatabase.getInstance().getReference().child(guild.getLongID()+"").child("users");
    }
}
