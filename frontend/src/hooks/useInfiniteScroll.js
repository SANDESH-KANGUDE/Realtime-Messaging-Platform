import { useEffect, useRef, useState } from 'react';

export const useInfiniteScroll = (callback, hasMore, isLoading) => {
  const containerRef = useRef(null);
  const [prevScrollHeight, setPrevScrollHeight] = useState(0);

  const handleScroll = () => {
    const container = containerRef.current;
    if (!container || isLoading || !hasMore) return;

    // Detect when user scrolls near the top of the container
    if (container.scrollTop <= 10) {
      setPrevScrollHeight(container.scrollHeight);
      callback();
    }
  };

  useEffect(() => {
    const container = containerRef.current;
    if (container) {
      container.addEventListener('scroll', handleScroll);
    }
    return () => {
      if (container) {
        container.removeEventListener('scroll', handleScroll);
      }
    };
  }, [hasMore, isLoading, callback]);

  // Adjust scroll position after messages load to prevent view jumping
  useEffect(() => {
    const container = containerRef.current;
    if (container && prevScrollHeight > 0 && !isLoading) {
      const scrollDiff = container.scrollHeight - prevScrollHeight;
      container.scrollTop = scrollDiff;
      setPrevScrollHeight(0);
    }
  }, [isLoading, prevScrollHeight]);

  return containerRef;
};

export default useInfiniteScroll;
