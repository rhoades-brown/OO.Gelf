import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

public class OOResource {
    private String name;
    private long currentUsage;
    private long peakUsage;
    private long limit;

    public OOResource(){
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        name = "Heap";
        currentUsage = heapMemoryUsage.getCommitted();
        peakUsage = -1;
        limit = heapMemoryUsage.getMax();
    }

    public OOResource(MemoryPoolMXBean PoolMXBean){

        name = PoolMXBean.getName();
        currentUsage = PoolMXBean.getUsage().getUsed();
        peakUsage = PoolMXBean.getPeakUsage().getUsed();
        limit = PoolMXBean.getPeakUsage().getMax();

    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public long getCurrentUsage(){return  currentUsage;}

    @JsonProperty
    public long getPeakUsage(){
        return peakUsage;
    }

    @JsonProperty
    public long getLimit(){return limit;}
}
