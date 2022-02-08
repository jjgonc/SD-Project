package Server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import Model.Frame;
import Model.Frame.Tag;

public class TaggedConnection implements AutoCloseable {
    Socket socket;
    ReentrantLock rl = new ReentrantLock();
    ReentrantLock wl = new ReentrantLock();
    DataOutputStream dos;
    DataInputStream dis;

    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void send(Frame frame) throws IOException {
        try {
            wl.lock();
            this.dos.writeUTF(frame.tag.name());
            this.dos.writeInt(frame.data.length);
            this.dos.write(frame.data);
            this.dos.flush();
        } finally {
            wl.unlock();
        }
    }

    public void send(Tag tag, byte[] data) throws IOException {
        this.send(new Frame(tag, data));
    }

    public Frame receive() throws IOException {
        Tag tag = null;
        byte[] data = null;
        String username = null;
        try {
            rl.lock();
            tag = Tag.valueOf(dis.readUTF());
            int n = this.dis.readInt();
            data = new byte[n];
            this.dis.readFully(data);

        }catch (EOFException e){
                    this.close();
                    return null;
        } finally {
            rl.unlock();
        }

        return new Frame(tag, data);
    }

    @Override
    public void close() throws IOException {
        this.dis.close();
        this.dos.close();
    }
}
