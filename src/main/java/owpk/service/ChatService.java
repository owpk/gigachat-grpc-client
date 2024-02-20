package owpk.service;

import gigachat.v1.Gigachatv1;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import owpk.Application;
import owpk.Constants;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static owpk.Constants.CHAT_ROLE;
import static owpk.Constants.USER_ROLE;

@Singleton
// TODO json storage
public class ChatService {
    private static final String CHAT_FILE = "chat";
    private static final String ROLE_USER_PAT = "[[" + USER_ROLE + "]]";
    private static final String ROLE_CHAT_PAT = "[[" + CHAT_ROLE + "]]";
    private static final Path CHAT_FILE_PATH = Paths.get(Application.appHome, CHAT_FILE);

    private final GigaChatGRpcClient gigaChatGRpcClient;

    @Inject
    public ChatService(GigaChatGRpcClient gigaChatGRpcClient, SettingsStore settingsStore) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        settingsStore.validate();
        try {
            if (!Files.exists(CHAT_FILE_PATH))
                Files.createFile(CHAT_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void chat(String query) {
        var response = gigaChatGRpcClient.chat(buildRequest(query));
        response.getAlternativesList().forEach(a -> System.out.print(a.getMessage().getContent()));
    }

    public void chatStream(String query) {
        var iter = gigaChatGRpcClient.chatStream(buildRequest(query));
        while (iter.hasNext()) {
            var msg = iter.next();
            System.out.print(msg.getAlternativesList().stream().map(a -> a.getMessage().getContent())
                    .collect(Collectors.joining(" ")));
        }
    }

    private Gigachatv1.ChatRequest buildRequest(String query) {
        persistContentToHistory(query.getBytes(), ROLE_USER_PAT);
        return Gigachatv1.ChatRequest.newBuilder()
                .setModel("GigaChat:latest")
                .addAllMessages(readLastMessages(10))
                .addMessages(Gigachatv1.Message.newBuilder()
                        .setRole(USER_ROLE)
                        .setContent(query)
                        .build())
                .build();
    }

    private void persistContentToHistory(byte[] content, String role) {
        try {
            var roleBytes = (role + "\n").getBytes();
            byte[] composedArray = new byte[content.length + roleBytes.length];
            System.arraycopy(roleBytes, 0, composedArray, 0, roleBytes.length);
            System.arraycopy(content, 0, composedArray, roleBytes.length, content.length);
            Files.write(CHAT_FILE_PATH, composedArray, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Gigachatv1.Message> readLastMessages(Integer linesCount) {
        int lines = linesCount;
        int readLines = 0;

        var messages = new ArrayList<Gigachatv1.Message>();

        try (var raf = new RandomAccessFile(CHAT_FILE_PATH.toFile(), "r")) {

            long fileLength = CHAT_FILE_PATH.toFile().length() - 1;
            raf.seek(fileLength);

            var lineBuilder = new StringWriter();
            for (long pointer = fileLength; pointer >= 0; pointer--) {
                var linesList = new ArrayList<String>();
                raf.seek(pointer);
                char c;
                c = (char) raf.read();
                lineBuilder.append(c);

                if (c == '\n' || c == '\r') {
                    var stringLine = lineBuilder.getBuffer().reverse().toString();
                    if (stringLine.startsWith(ROLE_USER_PAT)) {
                        var rpcMsg = buildMessage(String.join("", linesList), USER_ROLE);
                        messages.add(rpcMsg);
                        linesList = new ArrayList<>();
                    } else if (stringLine.startsWith(ROLE_CHAT_PAT)) {
                        var rpcMsg = buildMessage(String.join("", linesList), CHAT_ROLE);
                        messages.add(rpcMsg);
                        linesList = new ArrayList<>();
                    }
                    readLines++;
                    linesList.add(stringLine);
                    lineBuilder.flush();
                    if (readLines == lines)
                        break;
                }
                fileLength = fileLength - pointer;
            }

            return messages;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Gigachatv1.Message buildMessage(String content, String role) {
        return Gigachatv1.Message.newBuilder()
                .setRole(role)
                .setContent(content)
                .build();
    }

}
