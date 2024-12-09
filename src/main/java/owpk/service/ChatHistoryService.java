package owpk.service;

import static owpk.GigaChatConstants.MessageRole.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.io.ByteStreams;

import lombok.extern.slf4j.Slf4j;
import owpk.model.ChatMessage;
import owpk.properties.concrete.MainProps;
import owpk.storage.Storage;
import owpk.utils.FileUtils;

@Slf4j
public class ChatHistoryService {
    private static final String ROLE_PREFIX = "## [[_Role: ";
    private static final String ROLE_SUFFIX = " ]]";
    private static final String ROLE_USER_PAT = formatRoll(USER.getValue());
    private static final String ROLE_CHAT_PAT = formatRoll(ASSISTANT.getValue());

    private final Storage storage;
    private final MainProps mainProps;

    // TODO check chat history mode, create according storage
    public ChatHistoryService(Storage storage, MainProps mainProps) {
        this.mainProps = mainProps;
        this.storage = mainProps.getStorage();
        createOrGetLastChat(this.storage);
    }

    public String createOrGetLastChat(Storage storage) {
        var chatHome = mainProps.getProperty(MainProps.DEF_CHATS_HISTORY_HOME);
        var lastChat = mainProps.getProperty(MainProps.DEF_CURRENT_CHAT_NAME);
        if (lastChat == null || lastChat.isBlank()) {
            lastChat = createFileName();
            mainProps.setProperty(MainProps.DEF_CURRENT_CHAT_NAME, lastChat);
            storage.createFileOrDirIfNotExists(chatHome + lastChat);
        }
        return lastChat;
    }

    public String createNewChat() {
        log.info("Creating new chat...");
        String fileName = createFileName();
        mainProps.setProperty(MainProps.DEF_CHATS_HISTORY_HOME, fileName);
        storage.createFileOrDirIfNotExists(fileName);
        log.info("New chat created: {}", fileName);
        return fileName;
    }

    public List<ChatMessage> readLastMessages(boolean reversed, boolean all) {
        return readLastMessages(it -> false, reversed, all);
    }

    public List<ChatMessage> readLastMessages(int msgCount, boolean reversed, boolean all) {
        return readLastMessages(it -> it >= msgCount, reversed, all);
    }

    public void persistContentToHistory(String content, String role) {
        var roleString = formatRoll(role);
        var body = (content + "\n").getBytes(StandardCharsets.UTF_8);
        var roleBytes = (roleString + "\n").getBytes();
        byte[] composedArray = new byte[body.length + roleBytes.length];
        System.arraycopy(roleBytes, 0, composedArray, 0, roleBytes.length);
        System.arraycopy(body, 0, composedArray, roleBytes.length, body.length);
        storage.saveContent(getCurrentFileName(), composedArray, true);
    }

    public String getCurrentFileName() {
        return mainProps.getProperty(MainProps.DEF_CHATS_HISTORY_HOME) + mainProps.getProperty(MainProps.DEF_CURRENT_CHAT_NAME);
    }

    public void clearChatHistory() {
        storage.saveContent(getCurrentFileName(), new byte[0], false);
    }

    public List<ChatMessage> readAllMessages() {
        throw new RuntimeException("Not implemented yet");
    }

    private List<ChatMessage> readLastMessages(Predicate<Integer> predicate, boolean reversed, boolean all) {
        var messages = new ArrayList<ChatMessage>();
        var data = storage.getContent(getCurrentFileName());
        var chatTempFile = FileUtils.createTempFile(data, "chat_history" + UUID.randomUUID());
        try (var raf = new RandomAccessFile(chatTempFile.toFile(), "r")) {
            long fileLength = data.length;
            raf.seek(fileLength);

            var linesList = new ArrayList<String>();
            var byteArrayOut = ByteStreams.newDataOutput();

            for (long pointer = fileLength; pointer >= 0; pointer--) {
                raf.seek(pointer);
                byte b = (byte) raf.read();
                char c = (char) b;

                if (pointer == 0)
                    byteArrayOut.write(b);

                if (c == '\r' || c == '\n' || pointer == 0) {
                    var body = byteArrayOut.toByteArray();
                    reverse(body);
                    var stringLine = new String(body);

                    if (stringLine.startsWith(ROLE_USER_PAT)) {
                        linesList = getStrings(USER.getValue(),
                                linesList, messages);
                    } else if (stringLine.startsWith(ROLE_CHAT_PAT)) {
                        linesList = getStrings(ASSISTANT.getValue(),
                                linesList, messages);
                    } else if (stringLine.startsWith(ROLE_PREFIX)) {
                        if (all) linesList = getStrings(defineRole(stringLine), linesList, messages);
                        else linesList = new ArrayList<>();
                    } else {
                        linesList.add(stringLine + "\n");
                    }

                    byteArrayOut = ByteStreams.newDataOutput();

                    if (predicate.test(messages.size()))
                        break;
                } else
                    byteArrayOut.write(b);
                fileLength = fileLength - pointer;
            }
            if (reversed)
                Collections.reverse(messages);
            return messages;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String defineRole(String role) {
        return role.substring(role.indexOf("{") + 1, role.lastIndexOf("}")).trim();
    }

    private String createFileName() {
        return String.format("chat-%s-%s.md",
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(System.currentTimeMillis()),
                UUID.randomUUID());
    }

    private static String formatRoll(String pattern) {
        return ROLE_PREFIX + "{" + pattern + "}" + ROLE_SUFFIX;
    }

    private static ArrayList<String> getStrings(String user, ArrayList<String> linesList,
                                                ArrayList<ChatMessage> messages) {
        Collections.reverse(linesList);
        var rpcMsg = new ChatMessage(user,
                String.join("", linesList));
        messages.add(rpcMsg);
        linesList = new ArrayList<>();
        return linesList;
    }

    private static void reverse(byte[] array) {
        if (array == null)
            return;
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

}
