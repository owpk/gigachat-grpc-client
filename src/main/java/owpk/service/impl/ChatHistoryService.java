package owpk.service.impl;

import static owpk.GigaChatConstants.MessageRole.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.io.ByteStreams;

import lombok.extern.slf4j.Slf4j;
import owpk.Application;
import owpk.model.ChatMessage;
import owpk.settings.main.MainSettingField;
import owpk.storage.LocalStorage;
import owpk.storage.Storage;
import owpk.storage.app.MainSettingsStore;
import owpk.utils.FileUtils;

@Slf4j
public class ChatHistoryService {
    private static final String ROLE_PREFIX = "## [[_Role: ";
    private static final String ROLE_SUFFIX = " ]]";
    private static final String ROLE_USER_PAT = formatRoll(USER.getValue());
    private static final String ROLE_CHAT_PAT = formatRoll(ASSISTANT.getValue());

    private final Storage storage;
    private final MainSettingsStore mainSettingsStore;

    public ChatHistoryService(MainSettingsStore mainSettingsStore) {
        Path CHAT_FILE_ROOT = Paths.get(Application.APP_HOME_DIR.toString(), "chats");
        this.mainSettingsStore = mainSettingsStore;
        this.storage = new LocalStorage(CHAT_FILE_ROOT);
    }

    private String createFileName() {
        return String.format("chat-%s-%s.md",
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(System.currentTimeMillis()),
                UUID.randomUUID());
    }

    public String createNewChat() {
        log.info("Creating new chat...");

        String fileName = createFileName();
        mainSettingsStore.setProperty(MainSettingField.CURRENT_CHAT.getPropertyKey(), fileName);
        storage.createFile(fileName);

        log.info("New chat created: {}", fileName);
        return fileName;
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

    public List<ChatMessage> readLastMessages(boolean reversed, boolean all) {
        return readLastMessages(it -> false, reversed, all);
    }

    public List<ChatMessage> readLastMessages(int msgCount, boolean reversed, boolean all) {
        return readLastMessages(it -> it >= msgCount, reversed, all);
    }

    private List<ChatMessage> readLastMessages(Predicate<Integer> predicate, boolean reversed, boolean all) {
        var messages = new ArrayList<ChatMessage>();
        var data = storage.readFile(getCurrentFileName());
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

    public void persistContentToHistory(String content, String role) {
        var roleString = formatRoll(role);
        var body = (content + "\n").getBytes(StandardCharsets.UTF_8);
            var roleBytes = (roleString + "\n").getBytes();
            byte[] composedArray = new byte[body.length + roleBytes.length];
            System.arraycopy(roleBytes, 0, composedArray, 0, roleBytes.length);
            System.arraycopy(body, 0, composedArray, roleBytes.length, body.length);
            storage.writeFile(getCurrentFileName(), composedArray, true);
    }

    public String getCurrentFileName() {
        var name = mainSettingsStore.getProperty(MainSettingField.CURRENT_CHAT.getPropertyKey());
        if (name == null || name.isBlank()) {
            var chatName = createNewChat();
            mainSettingsStore.setProperty(MainSettingField.CURRENT_CHAT.getPropertyKey(), chatName);
            return chatName;
        }
        return name;
    }

    public void clearChatHistory() {
        storage.writeFile(getCurrentFileName(), new byte[0], false);
    }
}
