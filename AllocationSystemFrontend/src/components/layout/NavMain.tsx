import { ChevronRight, type LucideIcon } from "lucide-react"
import { Link, useLocation } from "react-router-dom"
import { useState } from "react"

import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
  SidebarGroup,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
} from "@/components/ui/sidebar"

export type NavItem = {
  title: string
  url: string
  icon: LucideIcon
  isActive?: boolean
  items?: {
    title: string
    url: string
  }[]
}

export type NavGroup = {
  label: string
  items: NavItem[]
}

export function NavMain({
  groups,
}: {
  groups: NavGroup[]
}) {
  const location = useLocation()
  
  const initializeOpenItems = (groups: NavGroup[], pathname: string) => {
    const initialOpen = new Set<string>()
    groups.forEach((group) => {
      group.items.forEach((item) => {
        if (item.items?.length) {
          const hasActiveChild = item.items.some(
            (subItem) => pathname === subItem.url
          )
          // Always expand Base Data Management by default
          const isBaseData = item.url.startsWith("/base-data")
          if (hasActiveChild || isBaseData) {
            initialOpen.add(item.title)
          }
        }
      })
    })
    return initialOpen
  }
  
  const [openItems, setOpenItems] = useState<Set<string>>(() =>
    initializeOpenItems(groups, location.pathname)
  )

  const isItemActive = (item: NavItem) => {
    if (item.isActive !== undefined) {
      return item.isActive
    }
    if (item.items?.length) {
      return item.items.some((subItem) => location.pathname === subItem.url)
    }
    return location.pathname === item.url
  }

  const isSubItemActive = (subItemUrl: string) => {
    return location.pathname === subItemUrl
  }

  const isItemOpen = (itemTitle: string) => {
    return openItems.has(itemTitle)
  }

  return (
    <>
      {groups.map((group) => (
        <SidebarGroup key={group.label}>
          <SidebarGroupLabel>{group.label}</SidebarGroupLabel>
          <SidebarMenu>
            {group.items.map((item) => {
              const hasSubItems = item.items && item.items.length > 0
              const itemActive = isItemActive(item)
              const isOpen = hasSubItems ? isItemOpen(item.title) : false
              
              return (
                <Collapsible
                  key={item.title}
                  asChild
                  open={isOpen}
                  onOpenChange={(open) => {
                    if (hasSubItems) {
                      setOpenItems((prev) => {
                        const next = new Set(prev)
                        if (open) {
                          next.add(item.title)
                        } else {
                          next.delete(item.title)
                        }
                        return next
                      })
                    }
                  }}
                >
                  <SidebarMenuItem>
                    {hasSubItems ? (
                      <CollapsibleTrigger asChild>
                        <SidebarMenuButton
                          tooltip={item.title}
                          isActive={false}
                          className="group"
                        >
                          <item.icon />
                          <span>{item.title}</span>
                          <ChevronRight className="ml-auto transition-transform duration-200 group-data-[state=open]:rotate-90" />
                        </SidebarMenuButton>
                      </CollapsibleTrigger>
                    ) : (
                      <SidebarMenuButton
                        asChild
                        tooltip={item.title}
                        isActive={itemActive}
                      >
                        <Link to={item.url}>
                          <item.icon />
                          <span>{item.title}</span>
                        </Link>
                      </SidebarMenuButton>
                    )}
                    {hasSubItems ? (
                      <CollapsibleContent>
                        <SidebarMenuSub>
                          {item.items?.map((subItem) => (
                            <SidebarMenuSubItem key={subItem.title}>
                              <SidebarMenuSubButton
                                asChild
                                isActive={isSubItemActive(subItem.url)}
                              >
                                <Link to={subItem.url}>
                                  <span>{subItem.title}</span>
                                </Link>
                              </SidebarMenuSubButton>
                            </SidebarMenuSubItem>
                          ))}
                        </SidebarMenuSub>
                      </CollapsibleContent>
                    ) : null}
                  </SidebarMenuItem>
                </Collapsible>
              )
            })}
          </SidebarMenu>
        </SidebarGroup>
      ))}
    </>
  )
}
