package okp.nic.stun;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.util.UtilityException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class StunTest {
    public static void main(String[] args) throws UtilityException, IOException {
        MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
        // sendMH.generateTransactionID();

        ChangeRequest changeRequest = new ChangeRequest();
        sendMH.addMessageAttribute(changeRequest);

        byte[] data = sendMH.getBytes();


        DatagramSocket s = new DatagramSocket();
        s.setReuseAddress(true);

        DatagramPacket p = new DatagramPacket(data, data.length, InetAddress.getByName("stun.l.google.com"), 19302);
        s.send(p);

        DatagramPacket rp;

        rp = new DatagramPacket(new byte[32], 32);

        s.receive(rp);
        MessageHeader receiveMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingResponse);
        // System.out.println(receiveMH.getTransactionID().toString() + "Size:"
        // + receiveMH.getTransactionID().length);
        try {
            receiveMH.parseAttributes(rp.getData());
        } catch (MessageAttributeParsingException e) {
            e.printStackTrace();
        }
        MappedAddress ma = (MappedAddress) receiveMH
                .getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
        System.out.println(ma.getAddress() + " " + ma.getPort());
    }
}