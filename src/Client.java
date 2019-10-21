import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import static javafx.application.Platform.exit;

public class Client {
    static boolean run = true;
    Thread rThread;

    public Client() throws Exception {

        /* Used for initial input DUH */
        Scanner input = new Scanner(System.in);
        System.out.println("Username: ");
        String username = input.nextLine();
        System.out.println("Ip: ");
        String ip = input.nextLine();
        System.out.println("Port: ");
        int port = input.nextInt();

        /* Creation of server socket */
        InetSocketAddress serverAd = new InetSocketAddress(ip, port);




        /* Connection to the server */
        SocketChannel client = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(serverAd);

        /* Creation of the tread that receives messages from the server */
        rThread = new Thread(new receiveThread(client));
        rThread.start();

        /* Registers the selector to the client (This is in charge of what connection is currently being managed) */
        Selector selector = Selector.open();
        client.register(selector, client.validOps(), null);

        System.out.println("Chat started, type something and see what happens!");

        /* This is loop enables the sending of messages */
        boolean first = true;
        byte[] message;
        while (run){

            /* This dictates the state of the server */
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();

            /* Everything in here performs an action based on the state of a server */
            while (keyIterator.hasNext()) {
                SelectionKey currentKey = keyIterator.next();

                /* Quits if server is offline */
                if (currentKey.isConnectable()) {
                    if (!attemptConnect(currentKey)){exit();}
                }

                if (currentKey.isWritable()) {
                    /* This sends a message if we have one to send */
                    if (first) {
                        ByteBuffer buffer = ByteBuffer.wrap(username.getBytes());
                        client.write(buffer);
                        buffer.clear();
                        first = false;
                    }
                    /* This will hang and wait for a message to send if we dont already have one */
                    else {
                        String m = input.nextLine();
                        message = m.getBytes();

                        ByteBuffer buffer = ByteBuffer.wrap(message);
                        client.write(buffer);
                        buffer.clear();

                        /* Exits on 'exit' */
                        if (m.toLowerCase().equals("exit")) {
                            client.close();
                            run = false;
                            break;
                        }
                    }
                    Thread.sleep(33);
                }
                keyIterator.remove();
            }
        }
    }

    public boolean attemptConnect(SelectionKey key) throws Exception{
        SocketChannel channel = (SocketChannel) key.channel();
        while (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        return true;
    }
}
