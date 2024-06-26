package de.rayzs.pat.utils.configuration.yaml;

import org.yaml.snakeyaml.constructor.Constructor;
import java.nio.charset.StandardCharsets;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.representer.*;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.*;
import java.util.*;
import java.io.*;

public class YamlConfiguration extends ConfigurationProvider {

    private final ThreadLocal<Yaml> yaml = new ThreadLocal<Yaml>()
    {
        @Override
        protected Yaml initialValue()
        {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );

            Representer representer = new Representer( options )
            {
                {
                    representers.put( Configuration.class, new Represent()
                    {
                        @Override
                        public Node representData(Object data)
                        {
                            return represent( ( (Configuration) data ).self );
                        }
                    } );
                }
            };

            return new Yaml( new Constructor( new LoaderOptions() ), representer, options );
        }
    };

    @Override
    public void save(Configuration config, File file) throws IOException
    {
        try ( Writer writer = new OutputStreamWriter( new FileOutputStream( file ), StandardCharsets.UTF_8 ) )
        {
            save( config, writer );
        }
    }

    @Override
    public void save(Configuration config, Writer writer)
    {
        yaml.get().dump( config.self, writer );
    }

    @Override
    public Configuration load(File file) throws IOException
    {
        return load( file, null );
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException
    {
        try ( FileInputStream is = new FileInputStream( file ) )
        {
            return load( is, defaults );
        }
    }

    @Override
    public Configuration load(Reader reader)
    {
        return load( reader, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( reader, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }

    @Override
    public Configuration load(InputStream is)
    {
        return load( is, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(InputStream is, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( is, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }

    @Override
    public Configuration load(String string)
    {
        return load( string, null );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(String string, Configuration defaults)
    {
        Map<String, Object> map = yaml.get().loadAs( string, LinkedHashMap.class );
        if ( map == null )
        {
            map = new LinkedHashMap<>();
        }
        return new Configuration( map, defaults );
    }
}