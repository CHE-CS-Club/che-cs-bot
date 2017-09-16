package club.checs.csbot;

import com.google.firebase.database.*;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;

public class RoleManager {
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

    @EventSubscriber
    public void onUserReact(ReactionEvent e) {
        // TODO Somehow allow servers to create rules channels. For now, this one is che cs specific
        if (!e.getChannel().getStringID().equals("356898774569713664")) // #rules
            return;
        // Make sure the user actually added a reaction and that it was the correct one
        // TODO Check if the right emoji
        if (!e.getReaction().getUserReacted(e.getUser()))
            return;
        DatabaseReference userRef = usersRef.child(e.getUser().getStringID());
        userRef.child("accepted_rules").setValue(true);
    }

    @EventSubscriber
    public void onMessageSent(MessageEvent e) {
        if (e instanceof MessageDeleteEvent)
            return;
        // TODO Somehow allow servers to create verify channels. For now, this one is che cs specific
        if (!e.getChannel().getStringID().equals("358403953932894208")) // #verify
            return;

        // Make sure the message given is valid
        String messageS = e.getMessage().getContent();
        String[] split = messageS.split("/");
        if (split.length != 3) {
            RequestBuffer.request(() -> e.getAuthor().getOrCreatePMChannel().sendMessage("Uh oh, looks " +
                    "like your verfiy message didn't follow our format. To verify please follow the proper format. " +
                    "If you're having issues please direct message one of our officers."));
            e.getMessage().delete();
            return;
        }
        String firstName = split[0].trim();
        String lastName = split[1].trim();
        String email = split[2].trim();

        final IGuild guild = e.getGuild();
        final IMessage message = e.getMessage();

        // Try and set the users information
        DatabaseReference userRef = usersRef.child(message.getAuthor().getStringID());
        userRef.child("accepted_rules").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    if (snapshot.getValue() != null && (boolean) snapshot.getValue()) {
                        // Upload data
                        userRef.child("firstName").setValue(firstName);
                        userRef.child("lastName").setValue(lastName);
                        userRef.child("email").setValue(email);

                        // Set the nickname
                        guild.setUserNickname(message.getAuthor(), firstName);

                        List<IRole> existing = guild.getRolesForUser(e.getAuthor());
                        existing.add(guild.getRoleByID(356900030277222401L));
                        IRole[] roles = new IRole[existing.size()];
                        for (int i = 0; i < existing.size(); i++)
                            roles[i] = existing.get(i);

                        guild.editUserRoles(message.getAuthor(), roles);
                        message.delete();
                    } else {
                        RequestBuffer.request(() -> message.getAuthor().getOrCreatePMChannel().sendMessage("Uh oh, " +
                                "looks like you forgot to accept the rules. Go to #rules and click the " +
                                ":white_check_mark: to agree to them. After you accept try to verify again."));
                        message.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                RequestBuffer.request(() -> e.getAuthor().getOrCreatePMChannel().sendMessage("Uh oh, looks " +
                        "like our auto verifier failed to verify you. Please try again or direct message one of " +
                        "our officers if you're having issues."));
                error.toException().printStackTrace();
                e.getMessage().delete();
            }
        });
    }

}
