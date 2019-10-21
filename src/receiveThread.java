import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static javafx.application.Platform.exit;

public class receiveThread implements Runnable {
    private SocketChannel client;
    private Selector selector;

    public receiveThread(SocketChannel client) throws IOException {
        this.client = client;
        this.selector = Selector.open();
        this.client.register(selector, this.client.validOps(), null);
    }

    @Override
    public void run() {
        try {
            while (Client.run) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey currentKey = keyIterator.next();

                    if (currentKey.isConnectable()) {
                        if (attemptConnect(currentKey)){exit();}
                    }

                    if (currentKey.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);
                        String msg = new String(buffer.array()).trim();
                        System.out.println(msg);
                        buffer.clear();
                    }
                    keyIterator.remove();
                }
            }
        }
        catch (Exception e) {
            try {
                client.close();
            }
            catch (IOException e1) {

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
