package net.lakkie.chatter2;

public class ClientMessage {
        public final String type, content;
        public final String[] args;

        public ClientMessage(String type, String content, String[] args)
        {
            this.type = type;
            this.content = content;
            this.args = args;
        }

        public ClientMessage(String type, String content, C2Server server)
        {
            this.type = type;
            this.content = content;
            this.args = server.parseMessageArguments(content);
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            out.append("c2/");
            out.append(type);
            if (!content.isEmpty()) out.append(" ");
            out.append(content);
            return new String(out);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClientMessage)
            {
                ClientMessage that = (ClientMessage) obj;
                boolean bType = this.type.equals(that.type);
                boolean bContent = this.content.equals(that.content);
                boolean bArgs = this.args.length == that.args.length;
                if (bArgs) {
                    for (int i = 0; i < this.args.length; i++)
                    {
                        if (this.args[i].equals(that.args[i]))
                            bArgs = false;
                    }
                }
                return bType && bContent && bArgs;
            }
            else
            {
                return obj.equals(this);
            }
        }
    }