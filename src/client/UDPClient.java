package client;

import common.ExitCodeCommand;
import common.commands.CommandRequest;
import common.interaction.Response;
import common.utility.GZIPUtils;
import common.utility.Serializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;

public class UDPClient {

    private final String host;
    private final int port;
    private DatagramSocket socket;

    private static final int BUFFER_SIZE = 262144;
    private static final int TIMEOUT_MS = 2000;        
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 300;
    private static final int MAX_UDP_SIZE = 65000;
    private static final int COMPRESS_THRESHOLD = 8192;
    
    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        System.out.println("Клиент успешно запущен");
    }

    public void connect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT_MS);
    }

    public Response sendRequest(CommandRequest request) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                if (socket == null || socket.isClosed()) {
                    connect();
                }

                byte[] data = Serializer.serialize(request);

                
                if (data.length > COMPRESS_THRESHOLD) {
                    data = GZIPUtils.compress(data);
                    System.out.println("-> Запрос сжат GZIP (" + data.length + " байт | было " + Serializer.serialize(request).length + ")");
                }

                if (data.length > MAX_UDP_SIZE) {
                    System.out.println("Предупреждение: Запрос слишком большой (" + data.length + " байт), может быть потерян");
                }

                System.out.println("-> Попытка реконнекта:[" + (attempts + 1) + "/" + MAX_RETRIES + "] с повторной отправкой запроса "
                        + request.getNameOfCommand() + " (" + data.length + " байт)");

                InetAddress address = InetAddress.getByName(host);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
                socket.send(sendPacket);

                return receiveResponseFast();

            }
            catch (PortUnreachableException | SocketTimeoutException e) {
                attempts++;
                System.out.println("Сервер не отвечает. Попытка реконнекта " + attempts + "/" + MAX_RETRIES);
            }
            catch (java.net.ConnectException | java.net.NoRouteToHostException e) {
                attempts++;
                System.out.println("Нет соединения с сервером (возможно, сервер не запущен или проблема с сетью). Попытка реконнекта "
                        + attempts + "/" + MAX_RETRIES);
            }
            catch (java.net.UnknownHostException e) {
                System.out.println("Ошибка: Неизвестный хост " + host);
                return new Response(ExitCodeCommand.ERROR, "Неизвестный хост");
            }
            catch (Exception e) {
                attempts++;
                System.out.println("Неизвестная ошибка (" + e.getClass().getSimpleName() + "): "
                        + e.getMessage() + ". Попытка реконнекта " + attempts + "/" + MAX_RETRIES);
            }

            if (attempts < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {}
            }
        }

        System.out.println("Не удалось связаться с сервером после " + MAX_RETRIES + " попыток.");
        return new Response(ExitCodeCommand.ERROR, "Сервер временно недоступен.");
    }

    private Response receiveResponseFast() throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        try {
            socket.receive(receivePacket);

            byte[] data = new byte[receivePacket.getLength()];
            System.arraycopy(buffer, 0, data, 0, data.length);

            Response response = tryDeserialize(data);

            if (response == null) {
                try {
                    byte[] decompressed = GZIPUtils.decompress(data);
                    response = tryDeserialize(decompressed);
                    if (response != null) {
                        System.out.println("Ответ распакован GZIP");
                    }
                } catch (Exception ignored) {}
            }

            if (response != null) {
                System.out.println("<- Ответ получен (" + data.length + " байт)");
                return response;
            }
            return null;

        } catch (SocketTimeoutException e) {
            throw e;  
        }
    }

    private Response tryDeserialize(byte[] data) {
        try {
            return (Response) Serializer.deserialize(data);
        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}