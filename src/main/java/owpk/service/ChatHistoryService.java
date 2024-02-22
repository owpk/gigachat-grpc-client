package owpk.service;

import com.google.common.io.ByteStreams;
import io.micronaut.core.util.ArrayUtils;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.Application;
import owpk.GigaChatConstants;
import owpk.model.ChatMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Singleton
public class ChatHistoryService {
    private static final String ROLE_USER_PAT = formatRoll(GigaChatConstants.Role.USER);
    private static final String ROLE_CHAT_PAT = formatRoll(GigaChatConstants.Role.ASSISTANT);
    private static final String ROLE_PREFIX = "[[_Role: ";
    private static final String ROLE_SUFFIX = " ]]";

    // TODO exclude to some props
    private static final Path CHAT_FILE_PATH =
            Paths.get(Application.appHome, "chat");

    private static String formatRoll(String pattern) {
        return ROLE_PREFIX + "{" + pattern + "}" + ROLE_SUFFIX;
    }

    public ChatHistoryService() {
        try {
            if (!Files.exists(CHAT_FILE_PATH))
                Files.createFile(CHAT_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ChatMessage> readLastMessages() {
        return readLastMessages(it -> false);
    }

    public List<ChatMessage> readLastMessages(int msgCount) {
        return readLastMessages(it -> it <= msgCount);
    }

    private List<ChatMessage> readLastMessages(Predicate<Integer> predicate) {
        var messages = new ArrayList<ChatMessage>();

        try (var raf = new RandomAccessFile(CHAT_FILE_PATH.toFile(), "r")) {
            long fileLength = CHAT_FILE_PATH.toFile().length();
            raf.seek(fileLength);

            var lineBuilder = new StringWriter();
            var linesList = new ArrayList<String>();
            var byteArrayOut = ByteStreams.newDataOutput();

            for (long pointer = fileLength; pointer >= 0; pointer--) {
                raf.seek(pointer);
                byte b = (byte) raf.read();
                char c = (char) b;

                if (pointer == 0)
                    lineBuilder.append(c);

                if (c == '\r' || c == '\n' || pointer == 0) {
                    var body = byteArrayOut.toByteArray();
                    reverse(body);
                    var stringLine = new String(body);

                    if (stringLine.startsWith(ROLE_USER_PAT)) {
                        linesList = getStrings(GigaChatConstants.Role.USER, linesList, messages);
                    } else if (stringLine.startsWith(ROLE_CHAT_PAT)) {
                        linesList = getStrings(GigaChatConstants.Role.ASSISTANT, linesList, messages);
                    } else
                        linesList.add(stringLine);

                    byteArrayOut = ByteStreams.newDataOutput();

                    if (predicate.test(messages.size()))
                        break;
                } else
                    byteArrayOut.write(b);
                fileLength = fileLength - pointer;
            }
            return messages;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<String> getStrings(String user, ArrayList<String> linesList,
                                                ArrayList<ChatMessage> messages) {
        var rpcMsg = new ChatMessage(user,
                String.join("", linesList));
        messages.add(rpcMsg);
        linesList = new ArrayList<>();
        return linesList;
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
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


    public void persistContentToHistory(String content, String role) {
        var roleString = formatRoll(role);
        var body = (content + "\n").getBytes(StandardCharsets.UTF_8);
        try {
            var roleBytes = (roleString + "\n").getBytes();
            byte[] composedArray = new byte[body.length + roleBytes.length];
            System.arraycopy(roleBytes, 0, composedArray, 0, roleBytes.length);
            System.arraycopy(body, 0, composedArray, roleBytes.length, body.length);
            Files.write(CHAT_FILE_PATH, composedArray, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
